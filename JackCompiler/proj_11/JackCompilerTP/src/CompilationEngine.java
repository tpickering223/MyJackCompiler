
import java.io.*;

public class CompilationEngine {
	
	private File output = null;
	private VMWriter vmWrite;
	private JackTokenizer tokenizer;
	private KeyWord kw;
	private boolean subdone;
	private SymbolTable variables;
	private String fileName;
	private String functionName;
	private int labelCount;
	private boolean inExpression;
	private boolean dotCall;
	private boolean staticVar;
	
	public CompilationEngine(File source) throws IOException {
		fileName = source.getName();
		output = new File(source.getAbsolutePath().replace(".jack", ".vm"));
		
		System.out.println("Compilation file generated at path: " + source.getAbsolutePath());
		
		variables = new SymbolTable();
		vmWrite = new VMWriter(output);
		tokenizer = new JackTokenizer(source);
		
		labelCount = 0;
		subdone = false;
		inExpression = false;
		dotCall = false;
		staticVar = false;
		fileName = fileName.replace(".jack", "");
		
	}
	
	
	// Compiles class in jack syntax: class className { classVariables subroutines }
	public void compileClass() throws IOException {

		
		tokenizer.advance();
		if(tokenizer.tokenType() == Token.KEYWORD) {
			// Do nothing
		}
		else {
			throw new RuntimeException("Compilation Error: Expected Class name, but got: \"" + tokenizer.identifier() + "\" instead");           
		}
		
		tokenizer.advance();
		if(tokenizer.tokenType() == Token.IDENTIFIER) {
			// Do nothing
		}
		else {
			throw new RuntimeException("Expected class identifier.");
		}
		
		tokenizer.advance();
		if(tokenizer.tokenType() == Token.SYMBOL) {
			// Do nothing
		}
		else {
			throw new RuntimeException("No class braces detected!");
		}
		compileClassVarDec();
		
		while(!subdone)
			compileSubroutine();
		
		
		if(tokenizer.tokenType() == Token.SYMBOL) {
			// Do nothing
		}
		else {
			throw new RuntimeException("No class braces detected!");
		}
		
		
	}
	
	
	public void compileClassVarDec() throws IOException {
		tokenizer.advance();
		while(tokenizer.isStaticfield() && !tokenizer.identifier().equals("}")) {
			staticVar = true;
			compileClassVar();
			tokenizer.advance();
		}
		if(tokenizer.identifier().equals("}")) {
			subdone = true;						// Class braces indicate there are no functions in this class.
		}
		
	}
	
	public void compileClassVar() throws IOException {
		
		String name = "";
		String type = "";
		KIND keyword = KIND.NONE;
		
		if(tokenizer.identifier().equals("static")) {
			keyword = KIND.STATIC;
		}
		if(tokenizer.identifier().equals("field")) {
			keyword = KIND.FIELD;
		}
		
		tokenizer.advance();
		
		
		if(tokenizer.identifier().equals("int") || tokenizer.identifier().equals("boolean") || tokenizer.identifier().equals("char")) {
			type = tokenizer.identifier();
		}
		else if(tokenizer.tokenType() == Token.IDENTIFIER) {
			type = tokenizer.identifier();
		}
		
		tokenizer.advance();
		while(!tokenizer.identifier().equals(";")) {
			name = tokenizer.identifier();
			if(name.equals("") || type.equals("") || keyword == KIND.NONE)
				throw new RuntimeException("Error in tokenizing variable declaraion...Name: " + name + "\ntype: " + type + "\nKind: NONE");
			variables.define(name, type, keyword);

			tokenizer.advance();
			if(tokenizer.identifier().endsWith(",")) {
				tokenizer.advance();
			}
		}
		if(!tokenizer.identifier().equals(";")) {
			throw new RuntimeException("Error: Expected ';'");
		}
		
	
	}
	
	public void writeKeyword(String word) throws IOException {
	//	writeOut.write("<keyword> ");
	//	writeOut.write(word);
	//	writeOut.write(" </keyword>\n");
	}
	
	public void writeCM() throws IOException {
	//	writeOut.write("<symbol> ");
	//	writeOut.write(",");
	//	writeOut.write(" </symbol>\n");
		
	}
	
	public void writeSC() throws IOException {
	//	writeOut.write("<symbol> ");
	//	writeOut.write(";");
	//	writeOut.write(" </symbol>\n");
	}
	
	public void writeIdent(String word) throws IOException {
	//	writeOut.write("<identifier> ");
	//	writeOut.write(word);
	//	writeOut.write(" </identifier>\n");
	}
	
	public void compileSubroutine() throws IOException {
		String type = "";
		kw = tokenizer.getKeyWord();
		if(tokenizer.getKeyWord() == KeyWord.METHOD) {
			variables.define("this", fileName, KIND.ARG);
			tokenizer.advance();
		}
		else if(tokenizer.tokenType() == Token.KEYWORD) {
			tokenizer.advance();
		}
		staticVar = false;
		if(!tokenizer.identifier().equals("}"))
			variables.startSubRoutine();
	 
		// Check if function is a class method, if so put first argument as this pointer to instance object into the table.
		
		if(tokenizer.tokenType() == Token.KEYWORD) {
			type = tokenizer.identifier();
			tokenizer.advance();
		}
		else if(tokenizer.tokenType() == Token.IDENTIFIER) {
			type = tokenizer.identifier();
			
			tokenizer.advance();
		}
		if(tokenizer.tokenType() == Token.IDENTIFIER) {
			functionName = tokenizer.identifier();
		}
		tokenizer.advance();
		writeSymbol("(");
		
		tokenizer.advance();
		compileParameterList();
		if(tokenizer.identifier().equals(")"))
			writeSymbol(")");
		
		
		writeSymbol("{");
		tokenizer.advance();
		compileSubroutineBody();
		
		if(tokenizer.identifier().equals("}"))
			writeSymbol("}");
		else {
			tokenizer.advance();
			writeSymbol("}");
		}
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
		vmWrite.writeFunction(fileName + "." + functionName + " ", variables.varCount(KIND.VAR));
		if(kw == KeyWord.METHOD) {
			vmWrite.writePush(Segment.ARG, 0);
			vmWrite.writePop(Segment.POINTER, 0);
		}
		else if (kw == KeyWord.CONSTRUCTOR){
			vmWrite.writePush(Segment.CONST, variables.varCount(KIND.FIELD));
			vmWrite.writeCall("Memory.alloc", 1);
			vmWrite.writePop(Segment.POINTER, 0);
		}
		while(tokenizer.tokenType() == Token.KEYWORD) {
			compileStatements();
		}
	}
	
	public void writeSymbol(String symbol) throws IOException {
/*		writeOut.write("<symbol> ");
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
		*/
		
	}

	
	public void compileParameterList() throws IOException {
		String type = "";
		boolean same = false;
		while(!tokenizer.identifier().equals(")")) {
			if(tokenizer.tokenType() == Token.KEYWORD) {
				type = tokenizer.identifier();
				tokenizer.advance();
				same = true;
			}
			else if(tokenizer.tokenType() == Token.IDENTIFIER) {
				if(!same) {
					type = tokenizer.identifier();
					tokenizer.advance();
					same = true;
				}
				else {
					variables.define(tokenizer.identifier(), type, KIND.ARG);
					same = false;
				}
				
			}
			else if(tokenizer.identifier().equals(",")) {
				writeCM();
				tokenizer.advance();
			}
		}
	}
	
	public void compileVarDec() throws IOException {
		writeKeyword("var");	// Write var
		tokenizer.advance();
		String type = "";
		if(tokenizer.identifier().equals("int") || tokenizer.identifier().equals("boolean") || tokenizer.identifier().equals("char")) {
			type = tokenizer.identifier();
		}
		else if(tokenizer.tokenType() == Token.IDENTIFIER) {	// Write type or Object name
			type = tokenizer.identifier();
		}
		tokenizer.advance();
		while(!tokenizer.identifier().equals(";")) {
			String temp = tokenizer.identifier();
			variables.define(temp, type, KIND.VAR);
			tokenizer.advance();
			
			if(tokenizer.identifier().equals(",")) {
				writeCM();
				tokenizer.advance();
			}
		}
		if(tokenizer.identifier().equals(";")) {
			writeSC();
		}
		tokenizer.advance();
	}
	
	public void compileStatements() throws IOException {
		while(tokenizer.tokenType() == Token.KEYWORD && !tokenizer.identifier().equals("}")) {
			switch (KeyWord.valueOf(tokenizer.identifier().toUpperCase())) {
			case DO:
				compileDo();
				tokenizer.advance();
				tokenizer.setCallValue(false);
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
	}
	
	public void compileDo() throws IOException {

		writeKeyword("do");
		compileCall();
		writeSymbol(";");
		vmWrite.writePop(Segment.TEMP, 0);
	}
	
	public void compileCall() throws IOException {
		if(!dotCall) {
			tokenizer.advance();
		}
		
		String callName = tokenizer.identifier();
		int num = 0;
		String type = "";
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
				String call = callName;
				callName = tokenizer.identifier();
				type = variables.typeOf(call);
				if(type.equals("")) {
					callName = call + "." + callName;
				}
				else {
					num = 1;
					vmWrite.writePush(stackSegment(variables.kindOf(call)), variables.indexOf(call));
					callName = variables.typeOf(call) + "." + callName;
				}
				
				tokenizer.advance();	// (
				num = num + compileExpressionList(true);
				tokenizer.advance();	// )
				vmWrite.writeCall(callName, num);
				if(tokenizer.getNext().equals(";"))
					tokenizer.advance();
			}
			else if(tokenizer.tokenType() == Token.SYMBOL && tokenizer.identifier().equals("(")) {
				writeSymbol("(");
				vmWrite.writePush(Segment.POINTER, 0);
				num = compileExpressionList(true) + 1;
				writeSymbol(")");
				tokenizer.advance();
				vmWrite.writeCall(fileName + "." + callName, num);
			}
		}
	}
	
	public Segment stackSegment(KIND id) {
		if(id == KIND.FIELD)
			return Segment.THIS;
		if(id == KIND.ARG)
			return Segment.ARG;
		if(id == KIND.VAR)
			return Segment.LOCAL;
		if(id == KIND.STATIC)
			return Segment.STATIC;
		else {
			return Segment.NONE;
		}
	}
	
	public void compileLet() throws IOException {

		writeKeyword("let");				// let
		tokenizer.advance();
		writeIdent(tokenizer.identifier());	// VarName
		String variable = tokenizer.identifier();
		
		tokenizer.advance();
		if(tokenizer.identifier().equals("[")) {
			writeSymbol("[");
			Segment currentScope = stackSegment(variables.kindOf(variable));
			vmWrite.writePush(currentScope, variables.indexOf(variable));	// Look up scope of variable (segment) and push it for operation/calculation of the address.
			compileExpression(false);
			writeSymbol("]");				// Assume advance() was called.
			tokenizer.advance();
			vmWrite.writeArithmetic(Command.ADD);	// Calculate index of array from internal expression
			inExpression = true;
			
		}	
		
		writeSymbol("=");
		compileExpression(false);
		// In order to get above's expression after the '=' into the proper address, must pop the values from temp segment.
		if(inExpression) {
			vmWrite.writePop(Segment.TEMP, 0);	// Pop expression to Temp. i.e. value temp = expression
			vmWrite.writePop(Segment.POINTER, 1); 	// Put calculated address into that segment. that = [address] 
			vmWrite.writePush(Segment.TEMP, 0);	// Push temp onto stack
			vmWrite.writePop(Segment.THAT, 0);	// Pop temp into that segment's address such that that = [address] --> expression value.
			inExpression = false;
		}
		else {
			int index = variables.indexOf(variable);
			Segment scope = stackSegment(variables.kindOf(variable));
			vmWrite.writePop(scope, index);
		}
		writeSymbol(";");
	}
	public void compileWhile() throws IOException {

		String whileLabel = "WHILE_LABEL_" + labelCount++;
		String notWhile = "LABEL_" + labelCount++;
		writeKeyword("while");
		vmWrite.writeLabel(whileLabel);
		
		tokenizer.advance();
		
		writeSymbol("(");
		compileExpression(false);
		writeSymbol(")");
		
		vmWrite.writeArithmetic(Command.NOT);
		vmWrite.writeIf(notWhile);
		
		tokenizer.advance();
		writeSymbol("{");
		tokenizer.advance();
		compileStatements();
		writeSymbol("}");
		vmWrite.writeGoto(whileLabel);
		vmWrite.writeLabel(notWhile);

	}
	
	
	public void compileReturn() throws IOException {

		writeKeyword("return");
		boolean done = false;
		if(tokenizer.getNext().equals(";")) {
			vmWrite.writePush(Segment.CONST, 0);
			tokenizer.advance();
			writeSC();
		}
		
		else if(!tokenizer.identifier().equals(";")) {
			compileExpression(false);
			done = true;
		}
		if(done) {
			writeSymbol(";");
			
		}
		vmWrite.writeReturn();

	}
	public void compileIf() throws IOException {

		tokenizer.setCallValue(false);
		writeKeyword("if"); 
		tokenizer.advance();
		writeSymbol("(");
		compileExpression(false);
		writeSymbol(")");
		
		vmWrite.writeArithmetic(Command.NOT);
		String jumpElse = "LABEL_" + labelCount++;
		
		vmWrite.writeIf(jumpElse);
		
		tokenizer.advance();
		writeSymbol("{");
		tokenizer.advance();
		compileStatements();
		writeSymbol("}");
		String gotoOther = "LABEL_" + labelCount++;
		vmWrite.writeGoto(gotoOther);
		tokenizer.advance();
		vmWrite.writeLabel(jumpElse);
		if(tokenizer.identifier().equals("else")) {
			writeKeyword("else");
			tokenizer.advance();
			writeSymbol("{");
			tokenizer.advance();
			compileStatements();
			writeSymbol("}");
			tokenizer.advance();
		}

		vmWrite.writeLabel(gotoOther);
		if(tokenizer.identifier().equals("return")) {
			compileReturn();
		}
	}
	
	public void compileExpression(boolean isParameter) throws IOException {
		Command operationC = Command.ADD;
		compileTerm(isParameter);
		String operation = "";
		if(tokenizer.isOp()) {
			writeSymbol(tokenizer.identifier());
			switch(tokenizer.symbol()) {
				case '+':
					
					operationC = Command.ADD;
				break;
				case '-':
					
					operationC = Command.SUB;
				break;
			
				case '<':
					
					operationC = Command.LT;
				break;
				
				case '>':
					
					operationC = Command.GT;
				break;
				
				case '=':
					
					operationC = Command.EQ;
				break;
				case '&':
					
					operationC = Command.AND;
				break;
				case '|':
					
					operationC = Command.ADD;
				break;
				case '*':
					operation = "call Math.multiply 2";
				break;
				case '/':
					operation = "call Math.divide 2";
				break;
					default: 
						throw new RuntimeException("Invalid operator token: " + tokenizer.identifier());
			
			}
			compileTerm(isParameter);
			if(!operation.equals("")) {
				vmWrite.writeRaw(operation);
			}
			else {
				vmWrite.writeArithmetic(operationC);
			}
			
		}

	}
	
	public void compileTerm(boolean isParameter) throws IOException {
		tokenizer.advance();
		if(tokenizer.tokenType() == Token.INT_CONST) {
			
			
			vmWrite.writePush(Segment.CONST, tokenizer.numValue());
			
			tokenizer.advance();
			
			
		}
		else if(tokenizer.tokenType() == Token.STRING_CONST) {
			
			String str_const = tokenizer.identifier();
			vmWrite.writePush(Segment.CONST, str_const.length());
			vmWrite.writeCall("String.new", 1);
			
			compileString(str_const);
			
			tokenizer.advance();
			
		}
		else if(tokenizer.tokenType() == Token.IDENTIFIER && tokenizer.isArray() ) {
			writeIdent(tokenizer.identifier());
			Segment scope = stackSegment(variables.kindOf(tokenizer.identifier()));
			int id = variables.indexOf(tokenizer.identifier());
			vmWrite.writePush(scope, id);
			tokenizer.advance();
			writeSymbol("[");
			
			compileExpression(false);
			writeSymbol("]");
			vmWrite.writeArithmetic(Command.ADD);	// Calculate array address
				
			vmWrite.writePop(Segment.POINTER, 1);
			
			vmWrite.writePush(Segment.THAT, 0);
			tokenizer.advance();
			
		}
		else if(tokenizer.tokenType() == Token.IDENTIFIER && isParameter) {
			Segment scope = stackSegment(variables.kindOf(tokenizer.identifier()));
			int id = variables.indexOf(tokenizer.identifier());
			vmWrite.writePush(scope, id);
			tokenizer.advance();
		}
		else if(tokenizer.tokenType() == Token.IDENTIFIER && tokenizer.isCall()) {
			tokenizer.setCallValue(false);
			writeIdent(tokenizer.identifier());
			dotCall = true;
			compileCall();
			dotCall = false;
		}
		else if (tokenizer.tokenType() == Token.IDENTIFIER) {
			Segment scope = stackSegment(variables.kindOf(tokenizer.identifier()));
			int id = variables.indexOf(tokenizer.identifier());
			writeIdent(tokenizer.identifier());
			tokenizer.advance();
			vmWrite.writePush(scope, id);
		}
		else if (tokenizer.tokenType() == Token.SYMBOL && tokenizer.isUOP()) {
			writeSymbol(tokenizer.identifier());
			char op = tokenizer.identifier().charAt(0);
			compileTerm(isParameter);
			
			if(op == '~') {
				vmWrite.writeArithmetic(Command.NOT);
			}
			else {
				vmWrite.writeArithmetic(Command.NEG);
			}
			
		}
		else if (tokenizer.tokenType() == Token.KEYWORD && tokenizer.isKC()) {
			writeKeyword(tokenizer.identifier());
			compileKC();
			tokenizer.advance();
		}
		else if (tokenizer.tokenType() == Token.SYMBOL && tokenizer.identifier().equals("(")) {
			writeSymbol("(");
			compileExpression(isParameter);
			writeSymbol(")");
			tokenizer.advance();
			
		}
		
		

	}
	
	public int compileExpressionList(boolean isParameter) throws IOException {

		int localExpressions = 0;
		if(tokenizer.getNext().equals(")") || tokenizer.getNext().equals(");")) {
			isParameter = false;
			tokenizer.advance();
			return localExpressions;
		}
		localExpressions++;
		compileExpression(isParameter);
		while(tokenizer.identifier().equals(",")) {
			writeCM();
			compileExpression(isParameter);
			localExpressions++;
		}
		isParameter = false;
		return localExpressions;
		
		
	}
	
	public void compileKC() throws IOException {
		if(tokenizer.identifier().equals("true")) {
			vmWrite.writePush(Segment.CONST, 0);
			vmWrite.writePush(Segment.CONST, 0);
			vmWrite.writeArithmetic(Command.EQ);
		}
		else if(tokenizer.identifier().equals("false")) {
			vmWrite.writePush(Segment.CONST, 0);
			vmWrite.writePush(Segment.CONST, 1);
			vmWrite.writeArithmetic(Command.EQ);
		}
		else if(tokenizer.identifier().equals("null")) {
			vmWrite.writePush(Segment.CONST, 0);
		}
		else if(tokenizer.identifier().equals("this")) {
			vmWrite.writePush(Segment.POINTER, 0);
		}
	}
	
	public void compileString(String input) throws IOException {
		for(int i = 0; i < input.length(); i++) {
			int k = (int) input.charAt(i);
			vmWrite.writePush(Segment.CONST, k);
			vmWrite.writeCall("String.appendChar", 2);
		}
	}
	
	public void close() throws IOException {
		tokenizer.close();
		vmWrite.close();
	}
	
}

