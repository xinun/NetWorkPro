
import java.io.Serializable;

public class CalcExpr implements Serializable {

    double operand1;
    double operand2;
    char operator;

    public CalcExpr(double op1, char operator, double op2) {
        this.operand1 = op1;
        this.operator = operator;
        this.operand2 = op2;
    }

    @Override
    public String toString() {
        return operand1 + " " + operator + " " + operand2;
    }
}