import java.io.*;
import java.util.HashMap;
public class VMWriter {
	private FileWriter out = null;
	
	private static HashMap<Command, String> commands;
	private static HashMap<Segment, String> segments;
	public VMWriter(File output) throws IOException {
		out = new FileWriter(output);
		commands = new HashMap<Command, String>();
		segments = new HashMap<Segment, String>();
		initializeCommands();
		initializeSegments();
		
		
	}
	
	public void writePush(Segment type, int index) throws IOException {
		out.write("push " + segments.get(type) + " " + index + "\n");
	}
	
	
	public void writePop(Segment type, int index) throws IOException {
		out.write("pop " + segments.get(type) + " " + index + "\n");
	}
	
	public void writeArithmetic(Command operation) throws IOException {
		out.write(commands.get(operation) +"\n");
	}
	
	public void writeLabel(String label) throws IOException {
		out.write("label " + label + "\n");
	}
	
	public void writeGoto(String label) throws IOException {
		out.write("goto " + label+ "\n");
	}
	
	public void writeIf(String label) throws IOException {
		out.write("if-goto " + label+ "\n");
	}
	
	public void writeCall(String name, int nArgs) throws IOException {
		out.write("call " + name + " " +  nArgs + "\n"); 
	}
	
	public void writeFunction(String name, int nLocals) throws IOException {
		out.write("function " + name + nLocals + "\n");
	}

	public void writeReturn() throws IOException {
		out.write("return\n");
	}
	
	public void writeRaw(String input) throws IOException {
		out.write(input + "\n");
	}
	
	private void initializeCommands() {
		commands.put(Command.ADD, "add");
		commands.put(Command.SUB, "sub");
		commands.put(Command.EQ, "eq");
		commands.put(Command.GT, "gt");
		commands.put(Command.AND, "and");
		commands.put(Command.OR, "or");
		commands.put(Command.LT, "lt");
		commands.put(Command.NOT, "not");
		commands.put(Command.NEG, "neg");
	}
	
	private void initializeSegments() {
		segments.put(Segment.ARG, "argument");
		segments.put(Segment.LOCAL, "local");
		segments.put(Segment.CONST, "constant");
		segments.put(Segment.POINTER, "pointer");
		segments.put(Segment.THIS, "this");
		segments.put(Segment.THAT, "that");
		segments.put(Segment.STATIC, "static");
		segments.put(Segment.TEMP, "temp");
	}
	public void close() throws IOException {
		out.close();
	}
}

enum Segment {
	CONST, ARG, LOCAL, STATIC, POINTER, TEMP, THIS, THAT, INVALID, NONE
}

enum Command {
	ADD, SUB, EQ, GT, LT, AND, OR, NOT, NEG
}
