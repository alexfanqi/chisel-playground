// import Mill dependency
import mill._
import mill.define.Sources
import mill.modules.Util
import mill.scalalib.scalafmt.ScalafmtModule
import mill.scalalib.TestModule.ScalaTest
import mill.scalalib._
// support BSP
import mill.bsp._

import os._

object v {
  val scalaVersion = "2.13.14"
  val spire        = ivy"org.typelevel::spire:0.18.0"
  val evilplot     = ivy"io.github.cibotech::evilplot:0.9.0"
  val oslib        = ivy"com.lihaoyi::os-lib:0.9.1"
  val mainargs     = ivy"com.lihaoyi::mainargs:0.5.0"
  val chiselCrossVersions = Map(
    "6.4.0" -> (ivy"org.chipsalliance::chisel:6.4.0", ivy"org.chipsalliance:::chisel-plugin:6.4.0", ivy"edu.berkeley.cs::chiseltest:6.0.0")
  )
}

trait CommonModule extends ScalaModule with ScalafmtModule {
  override def scalaVersion = v.scalaVersion
  override def ivyDeps =
    super.ivyDeps() ++ Agg(
      v.chiselCrossVersions("6.4.0")._1
    )
  def repositoriesTask = T.task {
    Seq(
      coursier.MavenRepository("https://repo.scala-sbt.org/scalasbt/maven-releases"),
      coursier.MavenRepository("https://oss.sonatype.org/content/repositories/releases"),
      coursier.MavenRepository("https://oss.sonatype.org/content/repositories/snapshots")
    ) ++ super.repositoriesTask()
  }
  override def scalacOptions = Seq(
    "-language:reflectiveCalls",
    "-deprecation",
    "-feature",
    "-Xcheckinit"
  )
}

trait LabModule extends CommonModule with HasElaborate { m =>
  override def scalacPluginIvyDeps = Agg(
    v.chiselCrossVersions("6.4.0")._2
  )
  object test extends ScalaTests with CommonModule with TestModule.ScalaTest {
    override def sources = T.sources {
      Seq(PathRef(this.millSourcePath))
    }
    override def ivyDeps = super.ivyDeps() ++ Agg(
      v.chiselCrossVersions("6.4.0")._3
    )
  }
  override def target_dir = os.pwd / "rtl" / "generated" / this.millSourcePath.subRelativeTo(os.pwd)
}

object elaborator extends CommonModule {
  override def ivyDeps =
    super.ivyDeps() ++ Agg(
      v.mainargs
    )
  // override def mainClass = Some("lab.elaborate")
}

trait HasElaborate extends ScalaModule {
  def top_module: String
  def target_dir: os.Path
  def moduleDeps = Seq(elaborator)
  // TODO: use Chisel standard build stage pipelines
  def elaborate = T {
    upstreamCompileOutput()
    mill.util.Jvm.runSubprocess(
      elaborator.finalMainClass(),
      runClasspath().map(_.path),
      forkArgs(),
      forkEnv(),
      Seq(
        "--dir",
        T.dest.toString,
        "--module",
        top_module
      ),
      workingDir = os.pwd
    )
    PathRef(T.dest)
  }

  def verilog = T {
    os.proc(
      "firtool",
      elaborate().path / s"${top_module}.fir",
      "--disable-annotation-unknown",
      "-O=debug",
      "--split-verilog",
      "--preserve-values=named",
      "--output-annotation-file=mfc.anno.json",
      s"-o=${T.dest}"
    ).call(T.dest)
    os.makeDir.all(target_dir / os.up)
    os.symlink(target_dir, T.dest)
    PathRef(T.dest)
  }
}

object lab3 extends LabModule { override def top_module = "gcd.GCD" }
// object lab4 extends LabModule
// object lab5 extends LabModule
