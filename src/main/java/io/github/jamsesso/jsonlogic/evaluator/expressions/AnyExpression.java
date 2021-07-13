package io.github.jamsesso.jsonlogic.evaluator.expressions;

import io.github.jamsesso.jsonlogic.ast.JsonLogicArray;
import io.github.jamsesso.jsonlogic.evaluator.JsonLogicEvaluationException;
import io.github.jamsesso.jsonlogic.evaluator.JsonLogicEvaluator;
import io.github.jamsesso.jsonlogic.evaluator.JsonLogicExpression;
import io.github.jamsesso.jsonlogic.utils.ArrayLike;
import io.github.jamsesso.jsonlogic.JsonLogic;

public class AnyExpression implements JsonLogicExpression {
  public static final AnyExpression INSTANCE = new AnyExpression();

  private AnyExpression() {
    // Use INSTANCE instead.
  }

  @Override
  public String key() {
    return "any";
  }

  @Override
  public Object evaluate(JsonLogicEvaluator evaluator, JsonLogicArray arguments, Object data)
    throws JsonLogicEvaluationException {
    if (arguments.size() != 2) {
      throw new JsonLogicEvaluationException("any expects exactly 2 arguments");
    }

    Object maybeArray = evaluator.evaluate(arguments.get(0), data);

    if (!ArrayLike.isEligible(maybeArray)) {
      throw new JsonLogicEvaluationException("first argument to any must be a valid array");
    }

    ArrayLike array = new ArrayLike(maybeArray);

    if (array.isEmpty()) {
      return false;
    }

    for (Object item : array) {
      if(JsonLogic.truthy(evaluator.evaluate(arguments.get(1), item))) {
        return true;
      }
    }

    return false;
  }
}
