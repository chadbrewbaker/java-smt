import edu.nyu.acsys.CVC4.Expr;
import edu.nyu.acsys.CVC4.ExprManager;
import edu.nyu.acsys.CVC4.Kind;
import edu.nyu.acsys.CVC4.SmtEngine;
import edu.nyu.acsys.CVC4.Type;



public class Test {
	
	public static void main(String[] args) {		
		System.loadLibrary("cvc4jni");		
		Env env = new Env();
		Creator creator = new Creator(env);
		Type booleanType = env.exprManager.booleanType();
		
		Expr x = creator.exprManager.mkVar("x", booleanType);
		Expr y = creator.exprManager.mkVar("y", booleanType);
		
		creator.smtEngine.assertFormula(env.exprManager.mkExpr(Kind.OR, x, y));
		creator.smtEngine.checkSat();
	}		
}
class Env{
	
	public final ExprManager exprManager;		
	public Env() {
		exprManager = new ExprManager();
	}		
	
	public SmtEngine newSMTEngine() {
		System.out.println("***** creator Pass 3");
		SmtEngine smtEngine = new SmtEngine(exprManager);
		System.out.println("***** creator Pass 4");
		return smtEngine;
	}
	public ExprManager getExprManager() {
		return exprManager;
	}
}	

class Creator{		
	protected final SmtEngine smtEngine;	
	protected final ExprManager exprManager;
	public Creator(Env env) {
		System.out.println("***** creator Pass 0");
	    exprManager = env.getExprManager();
	    System.out.println("***** creator Pass 1");
	    smtEngine = env.newSMTEngine();			
	    System.out.println("***** creator Pass 2");
	}		
}