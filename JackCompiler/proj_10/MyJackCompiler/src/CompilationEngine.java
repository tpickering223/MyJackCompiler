
import java.io.*;

public class CompilationEngine {
	File input = null;
	File output = null;
	FileWriter writeOut = null;
	boolean hasAdv;
	int tab_counter;
	JackTokenizer tokenizer;
	boolean subdone;
	public CompilationEngine(File source) throws IOException {
		input = source;
		output = new File(input.getAbsolutePath().replace(".jack", "CE.xml"));
		writeOut = new FileWriter(output);
		System.out.println("Compilation file generated at path: " + input.getAbsolutePath());
		tab_counter = 0;
		tokenizer = new JackTokenizer(input);
		subdone = false;
		hasAdv = false;
	}
	
	public void compileClass() throws IOException {
		writeOut.write("<class>\n");
		tab_counter++;
		tokenizer.advance();
		if(tokenizer.tokenType() == Token.KEYWORD) {
			writeOut.write("<keyword> ");
			writeOut.write(tokenizer.identifier());
			writeOut.write(" </keyword>\n");
		}
		else {
			throw new RuntimeException("Compilation Error: Expected Class name, but got: \"" + tokenizer.identifier() + "\" instead");           
		}
		
		tokenizer.advance();
		if(tokenizer.tokenType() == Token.IDENTIFIER) {
			writeOut.write("<identifier> ");
			writeOut.write(tokenizer.identifier());
			writeOut.write(" </identifier>\n");
		}
		else {
			throw new RuntimeException("Expected class identifier.");
		}
		
		tokenizer.advance();
		if(tokenizer.tokenType() == Token.SYMBOL) {
			writeOut.write("<symbol> ");
			writeOut.write(tokenizer.symbol());
			writeOut.write(" </symbol>\n");
		}
		else {
			throw new RuntimeException("No class braces detected!");
		}
		compileClassVarDec();
		
		while(!subdone)
			compileSubroutine();
		
		
		if(tokenizer.tokenType() == Token.SYMBOL) {
			writeOut.write("<symbol> ");
			writeOut.write(tokenizer.symbol());
			writeOut.write(" </symbol>\n");
		}
		else {
			throw new RuntimeException("No class braces detected!");
		}
		writeOut.write(" </class>\n");
		tab_counter--;
	}
	
	
	public void compileClassVarDec() throws IOException {
		tokenizer.advance();
		while(tokenizer.isStaticfield()) {
			compileClassVar();
			tokenizer.advance();
		}
		
	}
	
	public void compileClassVar() throws IOException {
		writeOut.write("<classVarDec> \n");
		if(tokenizer.identifier().equals("static")) {
			writeKeyword("static");
		}
		if(tokenizer.identifier().equals("field")) {
			writeKeyword("field");
		}
		tokenizer.advance();
		if(tokenizer.identifier().equals("int") || tokenizer.identifier().equals("boolean") || tokenizer.identifier().equals("char")) {
			writeKeyword(tokenizer.identifier());
		}
		else if(tokenizer.tokenType() == Token.IDENTIFIER) {
			writeIdent(tokenizer.identifier());
		}
		
		tokenizer.advance();
		while(!tokenizer.identifier().equals(";")) {
			String temp = tokenizer.identifier();
			writeIdent(temp);

			tokenizer.advance();
			if(tokenizer.identifier().endsWith(",")) {
				writeCM();
				tokenizer.advance();
			}
		}
		if(tokenizer.identifier().equals(";")) {
			writeSC();
		}
		
		writeOut.write("</classVarDec> \n");
	
	}
	
	public void writeKeyword(String word) throws IOException {
		writeOut.write("<keyword> ");
		writeOut.write(word);
		writeOut.write(" </keyword>\n");
	}
	
	public void writeCM() throws IOException {
		writeOut.write("<symbol> ");
		writeOut.write(",");
		writeOut.write(" </symbol>\n");
		
	}
	
	public void writeSC() throws IOException {
		writeOut.write("<symbol> ");
		writeOut.write(";");
		writeOut.write(" </symbol>\n");
	}
	
	public void writeIdent(String word) throws IOException {
		writeOut.write("<identifier> ");
		writeOut.write(word);
		writeOut.write(" </identifier>\n");
	}
	
	public void compileSubroutine() throws IOException {
		writeOut.write("<subroutineDec> \n");
		tab_counter++;
		if(tokenizer.tokenType() == Token.KEYWORD) {
			writeKeyword(tokenizer.identifier());
			tokenizer.advance();
		}
		if(tokenizer.tokenType() == Token.KEYWORD) {
			writeKeyword(tokenizer.identifier());
			tokenizer.advance();
		}
		else if(tokenizer.tokenType() == Token.IDENTIFIER) {
			writeIdent(tokenizer.identifier());
			tokenizer.advance();
		}
		
		writeIdent(tokenizer.identifier());	// Function name.
		tokenizer.advance();
		
		writeSymbol("(");
		tokenizer.advance();
		compileParameterList();
		if(tokenizer.identifier().equals(")"))
			writeSymbol(")");
		writeOut.write(" <subroutineBody>\n");
		writeSymbol("{");
		tokenizer.advance();
		compileSubroutineBody();
		if(tokenizer.identifier().equals("}"))
			writeSymbol("}");
		else {
			tokenizer.advance();
			writeSymbol("}");
		}
		writeOut.write(" </subroutineBody>\n");
		writeOut.write(" </subroutineDec>\n");
		tokenizer.advance();
		if(tokenizer.identifier().equals("}")) {
			subdone = true;
		}
	
	}
	
	public void compileSubroutineBody() throws IOException {
		tokenizer.advance();
		while(tokenizer.identifier().equals("var")) {
			compileVarDec();
		}
		// ^ VarDec*
		while(tokenizer.tokenType() == Token.KEYWORD) {
			compileStatements();
		}
	}
	
	public void writeSymbol(String symbol) throws IOException {
		writeOut.write("<symbol> ");
		if(symbol.equals("<")) {
			 writeOut.write("&lt;");
			 
		 }
		else if(symbol.equals("&")) {
			writeOut.write("&amp;");
		}
		else if(symbol.equals(">")) {
			writeOut.write("&gt;");
		}
		else if(symbol.equals("\"")) {
			writeOut.write("&quot;");
		}
		else {
			writeOut.write(symbol);
		}
		writeOut.write(" </symbol>\n");
		
	}
	public void printTab() throws IOException {
		for(int i = 0; i < tab_counter; i++) {
			writeOut.write("  ");
		}
	}
	
	public void compileParameterList() throws IOException {
		writeOut.write("<parameterList>\n");
		while(!tokenizer.identifier().equals(")")) {
			if(tokenizer.tokenType() == Token.KEYWORD) {
				writeKeyword(tokenizer.identifier());
				tokenizer.advance();
			}
			else if(tokenizer.tokenType() == Token.IDENTIFIER) {
				writeIdent(tokenizer.identifier());
				tokenizer.advance();
			}
			else if(tokenizer.identifier().equals(",")) {
				writeCM();
				tokenizer.advance();
			}
		}
		writeOut.write(" </parameterList>\n");
	}
	
	public void compileVarDec() throws IOException {
		writeOut.write("<varDec>\n");
		writeKeyword("var");	// Write var
		tokenizer.advance();
		if(tokenizer.identifier().equals("int") || tokenizer.identifier().equals("boolean") || tokenizer.identifier().equals("char")) {
			writeKeyword(tokenizer.identifier());
		}
		else if(tokenizer.tokenType() == Token.IDENTIFIER) {	// Write type or Object name
			writeIdent(tokenizer.identifier());
		}
		tokenizer.advance();
		while(!tokenizer.identifier().equals(";")) {
			String temp = tokenizer.identifier();
			writeIdent(temp);

			tokenizer.advance();
			if(tokenizer.identifier().equals(",")) {
				writeCM();
				tokenizer.advance();
			}
		}
		if(tokenizer.identifier().equals(";")) {
			writeSC();
		}
		writeOut.write(" </varDec>\n");
		tokenizer.advance();
	}
	
	public void compileStatements() throws IOException {
		writeOut.write("<statements> \n");
		while(tokenizer.tokenType() == Token.KEYWORD && !tokenizer.identifier().equals("}")) {
			switch (KeyWord.valueOf(tokenizer.identifier().toUpperCase())) {
			case DO:
				compileDo();
				tokenizer.advance();
				break;
			case IF:
				compileIf();
				while(tokenizer.identifier().equals("if")) {
					compileIf();
				}
				break;
			case LET:
				compileLet();
				tokenizer.advance();
				break;
			case RETURN:
				compileReturn();
				tokenizer.advance();
				break;
			case WHILE:
				compileWhile();
				tokenizer.advance();
				break;
			default:
				throw new RuntimeException("Error in matching keywords in statement compilation!");
			
				
					
			}
		}
		writeOut.write(" </statements>\n");
	}
	
	public void compileDo() throws IOException {
		writeOut.write("<doStatement> \n");
		writeKeyword("do");
		compileCall();
		writeSymbol(";");
		writeOut.write(" </doStatement>\n");
	}
	
	public void compileCall() throws IOException {
		tokenizer.advance();
		while(!tokenizer.identifier().equals(";")) {
			if(tokenizer.tokenType() == Token.KEYWORD) {
				writeKeyword(tokenizer.identifier());
				tokenizer.advance();
			}
			else if(tokenizer.tokenType() == Token.IDENTIFIER) {
				writeIdent(tokenizer.identifier());
				tokenizer.advance();
			}
			else if(tokenizer.tokenType() == Token.SYMBOL && !tokenizer.identifier().equals("(")) {
				writeSymbol(tokenizer.identifier());
				tokenizer.advance();
			}
			else if(tokenizer.tokenType() == Token.SYMBOL && tokenizer.identifier().equals("(")) {
				writeSymbol("(");
				compileExpressionList();
				writeSymbol(")");
				tokenizer.advance();
			}
		}
	}
	
	public void compileLet() throws IOException {
		writeOut.write("<letStatement>\n");
		writeKeyword("let");				// let
		tokenizer.advance();
		writeIdent(tokenizer.identifier());	// VarName
		tokenizer.advance();
		if(tokenizer.identifier().equals("[")) {
			writeSymbol("[");
			compileExpression();
			writeSymbol("]");				// Assume advance() was called.
			tokenizer.advance();
		}	
		writeSymbol("=");
		compileExpression();
		writeSymbol(";");
		writeOut.write("</letStatement>\n");
	}
	public void compileWhile() throws IOException {
		writeOut.write("<whileStatement>\n");
		writeKeyword("while");
		tokenizer.advance();
		writeSymbol("(");
		compileExpression();
		writeSymbol(")");
		tokenizer.advance();
		writeSymbol("{");
		tokenizer.advance();
		compileStatements();
		writeSymbol("}");
		writeOut.write("</whileStatement>\n");
	}
	
	
	public void compileReturn() throws IOException {
		writeOut.write("<returnStatement>\n");
		writeKeyword("return");
		boolean done = false;
		if(tokenizer.getNext().equals(";")) {
			tokenizer.advance();
			writeSC();
		}
		
		else if(!tokenizer.identifier().equals(";")) {
			compileExpression();
			done = true;
		}
		if(done) {
			writeSymbol(";");
			
		}
		writeOut.write("</returnStatement>\n ");
	}
	public void compileIf() throws IOException {
		writeOut.write("<ifStatement>\n");
		
		writeKeyword("if"); 
		tokenizer.advance();
		writeSymbol("(");
		compileExpression();
		writeSymbol(")");
		tokenizer.advance();
		writeSymbol("{");
		tokenizer.advance();
		compileStatements();
		writeSymbol("}");
		tokenizer.advance();
		if(tokenizer.identifier().equals("else")) {
			writeKeyword("else");
			tokenizer.advance();
			writeSymbol("{");
			tokenizer.advance();
			compileStatements();
			writeSymbol("}");
			tokenizer.advance();
		}
		writeOut.write(" </ifStatement>\n");
		if(tokenizer.identifier().equals("return")) {
			compileReturn();
		}
	}
	
	public void compileExpression() throws IOException {
		writeOut.write("<expression>\n");
		compileTerm();
		if(tokenizer.isOp()) {
			writeSymbol(tokenizer.identifier());
			compileTerm();
		}
		writeOut.write(" </expression>\n");
	}
	
	public void compileTerm() throws IOException {
		writeOut.write("<term> \n");
		tokenizer.advance();
		if(tokenizer.tokenType() == Token.INT_CONST) {
			writeOut.write("<integerConstant> ");
			writeOut.write(tokenizer.identifier());
			writeOut.write(" </integerConstant>\n");
			tokenizer.advance();
			
			
		}
		else if(tokenizer.tokenType() == Token.STRING_CONST) {
			writeOut.write("<stringConstant> ");
			writeOut.write(tokenizer.identifier());
			writeOut.write("</stringConstant>\n");
			tokenizer.advance();
			
		}
		else if(tokenizer.tokenType() == Token.IDENTIFIER && tokenizer.isArray() ) {
			writeIdent(tokenizer.identifier());
			tokenizer.advance();
			writeSymbol("[");
			compileExpression();
			writeSymbol("]");
			tokenizer.advance();
			
		}
		else if(tokenizer.tokenType() == Token.IDENTIFIER && tokenizer.isCall()) {
			writeIdent(tokenizer.identifier());
			tokenizer.advance();
			while(!tokenizer.identifier().equals(")") && !tokenizer.identifier().equals(";")) {
				if(tokenizer.tokenType() == Token.KEYWORD) {
					writeKeyword(tokenizer.identifier());
					tokenizer.advance();
				}
				else if(tokenizer.tokenType() == Token.IDENTIFIER) {
					writeIdent(tokenizer.identifier());
					tokenizer.advance();
				}
				else if(tokenizer.tokenType() == Token.SYMBOL && tokenizer.identifier().equals(",")) {
					writeOut.append("</term>\n");
					return;
				}
				else if(tokenizer.isOp()) {
					writeOut.append("</term>\n");
					return;
				}
				else if(tokenizer.tokenType() == Token.SYMBOL && !tokenizer.identifier().equals("(")) {
					
					writeSymbol(tokenizer.identifier());
					tokenizer.advance();
				}
				else if(tokenizer.tokenType() == Token.SYMBOL && tokenizer.identifier().equals("(")) {
					writeSymbol("(");
					compileExpressionList();
					writeSymbol(")");
					tokenizer.advance();
					
				}
			}
		}
		else if (tokenizer.tokenType() == Token.IDENTIFIER) {
			writeIdent(tokenizer.identifier());
			tokenizer.advance();
		}
		else if (tokenizer.tokenType() == Token.SYMBOL && tokenizer.isUOP()) {
			writeSymbol(tokenizer.identifier());
			compileTerm();
		}
		else if (tokenizer.tokenType() == Token.KEYWORD && tokenizer.isKC()) {
			writeKeyword(tokenizer.identifier());
			tokenizer.advance();
		}
		else if (tokenizer.tokenType() == Token.SYMBOL && tokenizer.identifier().equals("(")) {
			writeSymbol("(");
			compileExpression();
			writeSymbol(")");
			tokenizer.advance();
			
		}
		
		
		writeOut.write("</term>\n");
	}
	
	public void compileExpressionList() throws IOException {
		writeOut.write("<expressionList>\n");
		if(tokenizer.getNext().equals(")") || tokenizer.getNext().equals(");")) {
			writeOut.write("</expressionList>\n");
			tokenizer.advance();
			return;
		}
		compileExpression();
		while(tokenizer.identifier().equals(",")) {
			writeCM();
			compileExpression();
		}
		writeOut.write("</expressionList>\n");
		
	}
	
	public void close() throws IOException {
		writeOut.close();
		tokenizer.close();
	}
	
}

