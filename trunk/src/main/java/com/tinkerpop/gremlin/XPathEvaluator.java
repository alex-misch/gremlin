package com.tinkerpop.gremlin;

import com.tinkerpop.gremlin.statements.EvaluationException;
import com.tinkerpop.gremlin.statements.SyntaxException;
import com.tinkerpop.gremlin.statements.Tokens;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.JXPathInvalidSyntaxException;

import java.util.Iterator;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version 0.1
 */
public class XPathEvaluator {

    protected int codeDepth = 0;

    private GremlinPathContext gremlinPathContext;

    private enum ReturnType {
        LIST, ITERATOR
    }

    public XPathEvaluator() {
        this.gremlinPathContext = GremlinPathContext.newContext(null);
        this.gremlinPathContext.setLenient(false);
    }

    public void incrDepth() {
        this.codeDepth++;
    }

    public void decrDepth() {
        this.codeDepth--;
    }

    public int getDepth() {
        return this.codeDepth;
    }

    private Object evaluate(String xPathString, ReturnType type) throws SyntaxException, EvaluationException {
        try {
            if (this.gremlinPathContext.rootChanged()) {
                this.gremlinPathContext = GremlinPathContext.newContext(this.gremlinPathContext, this.gremlinPathContext.getRoot());
            }
            if (type == ReturnType.LIST) {
                List results = this.gremlinPathContext.selectNodes(xPathString);
                if (results.size() == 1)
                    this.setVariable(Tokens.LAST_VARIABLE, results.get(0));
                else
                    this.setVariable(Tokens.LAST_VARIABLE, results);
                return results;
            } else {
                return this.gremlinPathContext.iterate(xPathString);
            }
        } catch (JXPathInvalidSyntaxException e) {
            throw new SyntaxException(e.getMessage().replace("Invalid XPath:", "Invalid statement:"));
        } catch (JXPathException e) {
            throw new EvaluationException(e.getMessage().replace("Undefined variable: ", "undefined variable $").replace("Undefined function:", "undefined function"));
        } catch (SyntaxException e) {
            throw new SyntaxException(e.getMessage().replace("Invalid XPath:", "Invalid statement:"));
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(e.getMessage());
        }
    }

    public List evaluateList(String xPathString) throws SyntaxException, EvaluationException {
        return (List) this.evaluate(xPathString, ReturnType.LIST);
    }

    public Iterator evaluateIterator(String xPathString) throws SyntaxException, EvaluationException {
        return (Iterator) this.evaluate(xPathString, ReturnType.ITERATOR);
    }

    public void setVariable(String variable, Object value) {
        this.gremlinPathContext.setVariable(variable, value);
    }

    public Object getVariable(String variable) {
        return this.gremlinPathContext.getVariable(variable);
    }

    public void removeVariable(String variable) {
        this.gremlinPathContext.removeVariable(variable);
    }

    public void setRoot(Object root) {
        this.gremlinPathContext.setRoot(root);
    }

    public Object getRoot() {
        return this.gremlinPathContext;
    }

    public GremlinPathContext getGremlinPathContext() {
        return this.gremlinPathContext;
    }
}
