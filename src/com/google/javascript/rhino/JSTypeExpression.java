/*
 *
 * ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Rhino code, released
 * May 6, 1999.
 *
 * The Initial Developer of the Original Code is
 * Netscape Communications Corporation.
 * Portions created by the Initial Developer are Copyright (C) 1997-1999
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Bob Jervis
 *   Google Inc.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * the GNU General Public License Version 2 or later (the "GPL"), in which
 * case the provisions of the GPL are applicable instead of those above. If
 * you wish to allow use of your version of this file only under the terms of
 * the GPL and not to allow others to use your version of this file under the
 * MPL, indicate your decision by deleting the provisions above and replacing
 * them with the notice and other provisions required by the GPL. If you do
 * not delete the provisions above, a recipient may use your version of this
 * file under either the MPL or the GPL.
 *
 * ***** END LICENSE BLOCK ***** */

package com.google.javascript.rhino;

import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.StaticTypedScope;
import java.io.Serializable;

/**
 * When parsing a jsdoc, a type-annotation string is parsed to a type AST. Somewhat confusingly, we
 * use the Node class both for type ASTs and for the source-code AST. JSTypeExpression wraps a type
 * AST. During type checking, type ASTs are evaluated to JavaScript types.
 */
public final class JSTypeExpression implements Serializable {
  private static final long serialVersionUID = 1L;

  static final JSTypeExpression IMPLICIT_TEMPLATE_BOUND =
      new JSTypeExpression(new Node(Token.QMARK), "");

  static {
    IMPLICIT_TEMPLATE_BOUND.getRoot().setStaticSourceFile(
        new SimpleSourceFile("<IMPLICT_TEMPLATE_BOUND>", StaticSourceFile.SourceKind.STRONG));
  }

  /** The root of the AST. */
  private final Node root;

  /** The source name where the type expression appears. */
  private final String sourceName;

  public JSTypeExpression(Node root, String sourceName) {
    this.root = root;
    this.sourceName = sourceName;
  }

  /**
   * Make the given type expression into an optional type expression,
   * if possible.
   */
  public static JSTypeExpression makeOptionalArg(JSTypeExpression expr) {
    if (expr.isOptionalArg() || expr.isVarArgs()) {
      return expr;
    } else {
      Node equals = new Node(Token.EQUALS, expr.root);
      equals.clonePropsFrom(expr.root);
      return new JSTypeExpression(equals, expr.sourceName);
    }
  }

  /**
   * @return Whether this expression denotes an optional {@code @param}.
   */
  public boolean isOptionalArg() {
    return root.getToken() == Token.EQUALS;
  }

  /**
   * @return Whether this expression denotes a rest args {@code @param}.
   */
  public boolean isVarArgs() {
    return root.getToken() == Token.ITER_REST;
  }

  /** Evaluates the type expression into a {@code JSType} object. */
  public JSType evaluate(StaticTypedScope scope, JSTypeRegistry registry) {
    JSType type = registry.createTypeFromCommentNode(root, sourceName, scope);
    root.setJSType(type);
    return type;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof JSTypeExpression &&
        ((JSTypeExpression) other).root.isEquivalentTo(root);
  }

  @Override
  public int hashCode() {
    return root.hashCode();
  }

  /**
   * @return The source for this type expression.  Note that it will not
   * contain an expression if there's an @override tag.
   */
  public Node getRoot() {
    return root;
  }

  public String getSourceName() {
    return this.sourceName;
  }

  @Override
  public String toString() {
    return "type: " + root.toStringTree();
  }

  public JSTypeExpression copy() {
    return new JSTypeExpression(root.cloneTree(), sourceName);
  }

  /** Whether this expression is an explicit unknown template bound. */
  @SuppressWarnings("ReferenceEquality")
  public boolean isExplicitUnknownTemplateBound() {
    return this != IMPLICIT_TEMPLATE_BOUND && this.equals(IMPLICIT_TEMPLATE_BOUND);
  }
}
