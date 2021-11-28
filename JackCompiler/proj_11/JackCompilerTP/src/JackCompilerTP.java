import java.io.*;
public class JackCompilerTP {

	public static void main(String[] args) throws IOException {
		File source = null;
		File[] folder;
		if(args.length == 0) {
			System.out.println("No arguments detected! Proper usage: JackLexicalEngine Jackfile_or_directory");
		}
		
			
		source = new File(args[0]);		
		source.exists();
		
		
		if(source.isDirectory()) {
			folder = source.listFiles();
			for(File input : folder) {
				if(input.getAbsolutePath().endsWith(".jack")) {
					CompilationEngine compiler = new CompilationEngine(input);
					compiler.compileClass();
					compiler.close();
					System.out.println("Compilation complete");
				}
			}
		}
		else {
			source = new File(args[0]);
			CompilationEngine compiler = new CompilationEngine(source);
			compiler.compileClass();
			compiler.close();
			System.out.println("Compilation complete");

		}

	}
}
