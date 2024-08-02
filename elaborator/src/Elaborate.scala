package lab.elaborate

import chisel3.stage.ChiselGeneratorAnnotation
import chisel3.stage.phases.{Elaborate,Convert}
import firrtl.AnnotationSeq
import firrtl.options.TargetDirAnnotation
import mainargs._
import chisel3.Module

object Main {
  @main def elaborate(
    @arg(name = "dir") dir:        String,
    @arg(name = "module") top:    String
  ) = {
    lazy val mod_gen = () =>
      Class
        .forName(top)
        .getConstructor()
        .newInstance()
        .asInstanceOf[Module]

    Seq(
      new Elaborate,
      new Convert
    ).foldLeft(
      Seq(
        TargetDirAnnotation(dir),
        ChiselGeneratorAnnotation(mod_gen)
      ): AnnotationSeq
    ) { case (annos, phase) => phase.transform(annos) }
      .flatMap {
        case firrtl.stage.FirrtlCircuitAnnotation(circuit) =>
          os.write(os.Path(dir) / s"${top}.fir", circuit.serialize)
          None
        case _: chisel3.stage.DesignAnnotation[_] => None
        case a => Some(a)
      }
  }

  def main(args: Array[String]): Unit = ParserForMethods(this).runOrExit(args)
}
