all: build run

run:
	@cd src && java sudoku.SudokuTextInterface

build:
	@cd src && javac sudoku/SudokuTextInterface.java
