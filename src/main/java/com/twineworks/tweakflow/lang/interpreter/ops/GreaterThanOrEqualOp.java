/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Twineworks GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.twineworks.tweakflow.lang.interpreter.ops;

import com.twineworks.tweakflow.lang.ast.expressions.GreaterThanOrEqualNode;
import com.twineworks.tweakflow.lang.errors.LangError;
import com.twineworks.tweakflow.lang.errors.LangException;
import com.twineworks.tweakflow.lang.types.Type;
import com.twineworks.tweakflow.lang.types.Types;
import com.twineworks.tweakflow.lang.values.Value;
import com.twineworks.tweakflow.lang.values.Values;
import com.twineworks.tweakflow.lang.interpreter.EvaluationContext;
import com.twineworks.tweakflow.lang.interpreter.Stack;

import java.math.BigDecimal;

final public class GreaterThanOrEqualOp implements ExpressionOp {

  private final GreaterThanOrEqualNode node;
  private final ExpressionOp leftOp;
  private final ExpressionOp rightOp;

  public GreaterThanOrEqualOp(GreaterThanOrEqualNode node) {
    this.node = node;
    leftOp = node.getLeftExpression().getOp();
    rightOp = node.getRightExpression().getOp();

  }

  @Override
  public Value eval(Stack stack, EvaluationContext context) {

    Value left = leftOp.eval(stack, context);
    Value right = rightOp.eval(stack, context);

    ensureValidTypes(left, right, stack);

    if (left == Values.NIL) return right == Values.NIL ? Values.TRUE : Values.FALSE;
    if (right == Values.NIL) return Values.FALSE;

    Type leftType = left.type();
    Type rightType = right.type();

    if (leftType == Types.LONG){
      if (rightType == Types.LONG){
        return (left.longNum() >= right.longNum()) ? Values.TRUE : Values.FALSE;
      }
      if (rightType == Types.DOUBLE){
        return (left.longNum() >= right.doubleNum()) ? Values.TRUE : Values.FALSE;
      }
      if (rightType == Types.DECIMAL){
        return BigDecimal.valueOf(left.longNum()).compareTo(right.decimal()) >= 0 ? Values.TRUE : Values.FALSE;
      }
    }
    else if (leftType == Types.DOUBLE){
      if (rightType == Types.LONG){
        return (left.doubleNum() >= right.longNum()) ? Values.TRUE : Values.FALSE;
      }
      if (rightType == Types.DOUBLE){
        return (left.doubleNum() >= right.doubleNum()) ? Values.TRUE : Values.FALSE;
      }
      if (rightType == Types.DECIMAL){
        double d = left.doubleNum();
        if (Double.isFinite(d)){
          return BigDecimal.valueOf(left.doubleNum()).compareTo(right.decimal()) >= 0 ? Values.TRUE : Values.FALSE;
        }
        else{
          return (d == Double.POSITIVE_INFINITY) ? Values.TRUE : Values.FALSE;
        }
      }
    }
    else if (leftType == Types.DECIMAL){
      if (rightType == Types.LONG){
        return left.decimal().compareTo(BigDecimal.valueOf(right.longNum())) >= 0 ? Values.TRUE : Values.FALSE;
      }
      if (rightType == Types.DOUBLE){
        double d = right.doubleNum();
        if (Double.isFinite(d)){
          return left.decimal().compareTo(BigDecimal.valueOf(right.doubleNum())) >= 0 ? Values.TRUE : Values.FALSE;
        }
        else{
          return (d == Double.NEGATIVE_INFINITY) ? Values.TRUE : Values.FALSE;
        }
      }
      if (rightType == Types.DECIMAL){
        return left.decimal().compareTo(right.decimal()) >= 0 ? Values.TRUE : Values.FALSE;
      }
    }
    else if (leftType == Types.DATETIME && rightType == Types.DATETIME){
      return (left.dateTime().getInstant().compareTo(right.dateTime().getInstant()) >= 0) ? Values.TRUE : Values.FALSE;
    }
    throw new LangException(LangError.CAST_ERROR, "cannot compare types: "+leftType.name()+" and "+rightType.name(), stack, node.getSourceInfo());

  }

  private void ensureValidTypes(Value left, Value right, Stack stack){
    Type leftType = left.type();
    Type rightType = right.type();

    if ((left == Values.NIL || leftType.isNumeric()) &&
        (right == Values.NIL || rightType.isNumeric())){
      return;
    }

    if ((left == Values.NIL || leftType.isDateTime()) &&
        (right == Values.NIL || rightType.isDateTime())){
      return;
    }

    throw new LangException(LangError.CAST_ERROR, "cannot compare types "+left.type().name()+" and " + right.type().name(), stack, node.getSourceInfo());

  }

  @Override
  public boolean isConstant() {
    return leftOp.isConstant() && rightOp.isConstant();
  }

  @Override
  public ExpressionOp specialize() {

    Type leftType = node.getLeftExpression().getValueType();
    Type rightType = node.getRightExpression().getValueType();

    try {

      if (leftType == rightType){
        if (leftType == Types.DOUBLE){
          return new GreaterThanOrEqualOpDD(node);
        }
        if (leftType == Types.LONG){
          return new GreaterThanOrEqualOpLL(node);
        }
        if (leftType == Types.DECIMAL){
          return new GreaterThanOrEqualOpDecDec(node);
        }
        if (leftType == Types.DATETIME){
          return new GreaterThanOrEqualOpDtDt(node);
        }
      }
    } catch (LangException ignored){}

    return new GreaterThanOrEqualOp(node);
  }

  @Override
  public ExpressionOp refresh() {
    return new GreaterThanOrEqualOp(node);
  }


}
