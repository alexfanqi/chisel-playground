Chisel Project Template
=======================

A modified version of the [Chisel template](https://github.com/ucb-bar/chisel-template),
adding multiple-subproject support and a simple reusable elaborator using java reflection.

Contents at a glance:

* `.gitignore` - helps Git ignore junk like generated files, build products, and temporary files.
* `build.sc` - instructs mill to build the Chisel project
* `Makefile` - rules to call mill. just a template, not all rules are functioning
* `lab3/src/GCD.scala` - GCD source file
* `lab3/src/DecoupledGCD.scala` - another GCD source file
* `lab3/test/src/GCDSpec.scala` - GCD tester
* `elaborator/src/Elaborate.scala` - wrapper file to compile Chisel to SystemVerilog

Inherit LabModule to instantiate more subprojects, e.g.
```
object lab3 extends LabModule { override def top_module = "gcd.GCD" }
```

## Getting Started

First, install mill by referring to the documentation [here](https://com-lihaoyi.github.io/mill).

To run all tests in this design (recommended for test-driven development):
```bash
make test
```

To generate Verilog:
```bash
make verilog
```
