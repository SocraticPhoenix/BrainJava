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
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * A model used by the {@link ModelJavaBytecodeCompiler}. This model represents the tape, as well as the individual cells
 * and IO. It contains a {@link CellModel} and {@link IOModel}, and includes definitions of the bytecode necessary to:
 * <ul>
 *     <li>Advance the pointer</li>
 *     <li>Retreat the pointer</li>
 *     <li>Increment the cell under the pointer</li>
 *     <li>Decrement the cell under the pointer</li>
 *     <li>Store input in the cell under the pointer</li>
 *     <li>Output the value of the cell under the pointer</li>
 *     <li>Load the value of the current cell onto the JVM stack</li>
 * </ul>
 */
public interface TapeModel {

    CellModel cell();

    IOModel input();

    void initial(ClassWriter writer);

    void initialMethod(GeneratorAdapter gen);

    void advance(GeneratorAdapter gen);

    void retreat(GeneratorAdapter gen);

    void increment(GeneratorAdapter gen);

    void decrement(GeneratorAdapter gen);

    void input(GeneratorAdapter gen);

    void output(GeneratorAdapter gen);

    void get(GeneratorAdapter gen);

    void end(GeneratorAdapter main);

}
