
public class Variable {
	private String name;
	private KIND type;
	private int count;
	
	public Variable(String varName, KIND type, int scope) {
		name = varName;
		this.type = type;
		count = scope;
	}

	public KIND getType() {
		return type;
	}

	public int getCount() {
		return count;
	}

	public String getName() {
		return name;
	}
	
	
	
}

enum KIND {
	STATIC, FIELD, ARG, VAR, NONE;
}

