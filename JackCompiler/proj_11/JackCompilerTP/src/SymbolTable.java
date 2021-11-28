import java.util.HashMap;

public class SymbolTable {

	private HashMap<String, Variable> staticVars, functionVars;
	int functionVarCount, argVarCount, staticVarCount, fieldVarCount;
	
	/* Constructs the Symbol Table.
	 * Two Hashtables are created, representing class scope (staticVars) and local function scope (functionVars)
	 * Counts are kept for each KIND respective to their scope in order to keep track of how many exist and what scope
	 * the compiler is in.
	 * 
	 */
	public SymbolTable() {
		staticVars = new HashMap<String, Variable>();
		functionVars = new HashMap<String, Variable>();
		
		functionVarCount = 0;
		argVarCount = 0;
		staticVarCount = 0;
		fieldVarCount = 0;
		
	}
	
	
	public void startSubRoutine() {
		functionVars.clear(); // Ready HashMap for new functions
		functionVarCount = 0;
		argVarCount = 0;
	}
	
	// Returns the count of a given KIND in the Symbol Table.
	public int varCount(KIND type) {
		if(type == KIND.STATIC) 
			return staticVarCount;
		if(type == KIND.FIELD)
			return fieldVarCount;
		if(type == KIND.VAR)
			return functionVarCount;
		if(type == KIND.ARG)
			return argVarCount;
		else {
			throw new RuntimeException("Invalid Type Query!");
		}
	}
	
	// Add a new variable to the symbol table.
	public void define(String varName, String type, KIND kind) {
		int num;
		if(isFunctionVar(kind)) {
			if(kind == KIND.VAR) {
				num = functionVarCount;
				functionVarCount++;
				Variable newVar = new Variable(type, kind, num);
				functionVars.put(varName, newVar);
			}
			else {
			
				num = argVarCount++;
				Variable newVar = new Variable(type, kind, num);
				functionVars.put(varName, newVar);
			} 
		
		}
		else {
			if(kind == KIND.STATIC) {
				num = staticVarCount++;
				Variable newVar = new Variable(type, kind, num);
				staticVars.put(varName, newVar);
			}
			else {
				num = fieldVarCount++;
				Variable newVar = new Variable(type, kind, num);
				staticVars.put(varName, newVar);
			}
		}
	}
	
	public boolean isFunctionVar(KIND kind) {
		if(kind == KIND.VAR || kind == KIND.ARG)
			return true;
		else
			return false;
	}
	
	
	public KIND kindOf(String name) {
		Variable temp = staticVars.get(name);
		if(temp == null) {
			temp = functionVars.get(name);
			if(temp == null) {
				return KIND.NONE;
			}
			else 
				return temp.getType();
		}
		return temp.getType();
			
		
	}
	public String typeOf(String name) {
		Variable temp = staticVars.get(name);
		if(temp == null) {
			temp = functionVars.get(name);
			if(temp == null) {
				return "";
			}
			else 
				return temp.getName();
		}
		return temp.getName();
	}
	
	public int indexOf(String name) {
		Variable temp = staticVars.get(name);
		if(temp == null) {
			temp = functionVars.get(name);
			if(temp == null) {
				return -1;
			}
			else 
				return temp.getCount();
		}
		return temp.getCount();
	}
	
	
}
