PRJ ?= lab3
BUILD_DIR = ./rtl/generated/$(PRJ)
NVBOARD_HOME ?=

test:
	mill -i $(PRJ).test

verilog:
	$(call git_commit, "generate verilog")
	mill -i $(PRJ).elaborate

help:
	mill -i $(PRJ).runMain Elaborate --help

reformat:
	mill -i __.reformat

checkformat:
	mill -i __.checkFormat

bsp:
	mill -i mill.bsp.BSP/install

idea:
	mill -i mill.idea.GenIdea/idea

clean:
	-rm -rf $(BUILD_DIR)

.PHONY: test verilog help reformat checkformat clean

sim: verilog $(BUILD_DIR)
	verilator --trace --timing --assert --vpi -I$(BASE)/vsrc -MMD --prefix Vtop


-include ../Makefile
