package operator;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public enum Operator {
	ADD, SUB, MUL, DIV, MOD;
	
	private static final List<Operator> OPERATORS =
		  Collections.unmodifiableList(Arrays.asList(values()));
	private static final int SIZE = OPERATORS.size();
	private static final Random RANDOM = new Random();
	private static final Map<Operator, String> operatorsStr = 
			Collections.unmodifiableMap(
					new HashMap<Operator, String>(){
						private static final long serialVersionUID = 1L;
					{
					put(Operator.ADD, "+");
					put(Operator.SUB, "-");
					put(Operator.MUL, "*");
					put(Operator.DIV, "/");
					put(Operator.MOD, "%");
			}});
	private static final Map<String, Operator> strOperators = 
			Collections.unmodifiableMap(
					new HashMap<String, Operator>(){
						private static final long serialVersionUID = 1L;
						{
					put("+", Operator.ADD);
					put("-", Operator.SUB);
					put("*", Operator.MUL);
					put( "/", Operator.DIV);
					put( "%", Operator.MOD);
			}});
	
	public static Operator nextOperator()  {
		return OPERATORS.get(RANDOM.nextInt(SIZE));
	}
	
	public static Operator getOperator(int i){
		return OPERATORS.get(i%5);
	}
	public static int getIndex(Operator op){
		return OPERATORS.indexOf(op);
	}
	
	public static String getString(Operator op){
		return operatorsStr.get(op);
	}
	public static Operator getOperator(String opStr){
		return strOperators.get(opStr);
	}
}
