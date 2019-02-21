/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 socraticphoenix@gmail.com
 * Copyright (c) 2017 contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.gmail.socraticphoenix.brainjava.compiler;

import com.gmail.socraticphoenix.brainjava.ast.Node;
import com.gmail.socraticphoenix.brainjava.ast.WhileNode;

import java.util.List;

public interface BrainfuckVisitor {

    void visitStart() throws BrainfuckCompilationException;

    BrainfuckVisitor visitWhile(WhileNode node) throws BrainfuckCompilationException;

    void visitAdvance(Node node) throws BrainfuckCompilationException;

    void visitRetreat(Node node) throws BrainfuckCompilationException;

    void visitIncrement(Node node) throws BrainfuckCompilationException;

    void visitDecrement(Node node) throws BrainfuckCompilationException;

    void visitInput(Node node) throws BrainfuckCompilationException;

    void visitOutput(Node node) throws BrainfuckCompilationException;

    void visitEnd() throws BrainfuckCompilationException;

    default void visitNode(Node node) throws BrainfuckCompilationException {
        switch (node.kind()) {
            case ADVANCE:
                visitAdvance(node);
                break;
            case DECREMENT:
                visitDecrement(node);
                break;
            case INCREMENT:
                visitIncrement(node);
                break;
            case RETREAT:
                visitRetreat(node);
                break;
            case INPUT:
                visitInput(node);
                break;
            case OUTPUT:
                visitOutput(node);
                break;
            case WHILE_START:
                WhileNode whileNode = (WhileNode) node;
                BrainfuckVisitor visitor = visitWhile(whileNode);
                visitor.visitStart();
                visitor.visitAll(whileNode.getNodes());
                visitor.visitEnd();
                break;
        }
    }

    default void visitAll(List<Node> nodes) throws BrainfuckCompilationException {
        for(Node node : nodes) {
            this.visitNode(node);
        }
    }

}
