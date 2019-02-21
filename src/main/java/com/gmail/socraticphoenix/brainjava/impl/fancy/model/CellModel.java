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
package com.gmail.socraticphoenix.brainjava.impl.fancy.model;

import com.gmail.socraticphoenix.brainjava.impl.fancy.ModelJavaBytecodeCompiler;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * A model used by the {@link ModelJavaBytecodeCompiler}. It includes definitions of the bytecode necessary to:
 * <ul>
 *     <li>Decrement the cell</li>
 *     <li>Increment the cell</li>
 *     <li>Convert the cell data type to output</li>
 *     <li>Convert input to the cell data type</li>
 *     <li>Check if the cell is zero</li>
 *     <li>Load the zero/defualt value</li>
 * </ul>=
 */
public interface CellModel {

    String type();

    void initial(ClassWriter writer, MethodVisitor clinit);

    void increment(GeneratorAdapter gen);

    void decrement(GeneratorAdapter gen);

    void toCodepoint(GeneratorAdapter gen);

    void fromCodepoint(GeneratorAdapter gen);

    void defualtValue(GeneratorAdapter gen);

    void isZero(GeneratorAdapter gen, Label ifZero);

}
