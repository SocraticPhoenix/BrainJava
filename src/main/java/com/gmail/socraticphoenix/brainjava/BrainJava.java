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
package com.gmail.socraticphoenix.brainjava;

import com.gmail.socraticphoenix.brainjava.compiler.BrainfuckCompilationException;
import com.gmail.socraticphoenix.brainjava.compiler.BrainfuckCompiler;
import com.gmail.socraticphoenix.brainjava.compiler.BrainfuckPointedCompilationException;
import com.gmail.socraticphoenix.brainjava.impl.fancy.ModelJavaBytecodeCompiler;
import com.gmail.socraticphoenix.brainjava.impl.fancy.model.Models;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BrainJava implements Opcodes {

    public static void main(String[] in) throws BrainfuckCompilationException {

        List<String> args = new ArrayList<>();
        Map<String, Object> flags = new LinkedHashMap<>();
        flags.put("ucbound", Long.MAX_VALUE);
        flags.put("lcbound", Long.MIN_VALUE);
        flags.put("eof", 0);
        flags.put("io", "argument");

        for(String k : in) {
            if(k.startsWith("-")) {
                k = k.replaceFirst("-", "");
                String[] pieces = k.split("=");
                switch (pieces[0]) {
                    case "ucbound":
                    case "lcbound":
                        try {
                            flags.put(pieces[0], Long.parseLong(pieces[1]));
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid " + pieces[0] + " flag, expected integer number");
                            return;
                        }
                        break;
                    case "eof":
                        try {
                            flags.put(pieces[0], Integer.parseInt(pieces[1]));
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid eof flag, expected integer number");
                            return;
                        }
                        break;
                    case "io":
                        String type = pieces[1];
                        if(!type.equals("argument") && !type.equals("interactive")){
                            System.out.println("Invalid io flag, expected 'argument' or 'interactive'");
                            return;
                        }
                        flags.put(pieces[0], pieces[1]);
                        break;
                    default:
                        System.out.println("Unrecognized flag " + pieces[0]);
                        return;
                }
            } else {
                args.add(k);
            }
        }

        if (args.size() != 3) {
            System.out.println("Expected args of the form <class name> <target file> <program file>");
            return;
        }

        String clazz = args.get(0);
        String file = args.get(2);
        String targetFile = args.get(1);
        String prog = null;
        try {
            prog = new String(Files.readAllBytes(Paths.get(file)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BrainfuckCompilationException("Failed to read program", e);
        }

        System.out.println("Beginning compilation of " + clazz + "...");
        System.out.println("Compiling with flags:");
        for(Map.Entry<String, Object> flag : flags.entrySet()) {
            System.out.println(" " + flag.getKey() + ": " + flag.getValue());
        }
        ModelJavaBytecodeCompiler compiler = new ModelJavaBytecodeCompiler(Models.unboundedTape(clazz, Models.boundedCell((Long) flags.get("lcbound"), (Long) flags.get("ucbound")), flags.get("io").equals("argument") ? Models.argumentIO((Integer) flags.get("eof")) : Models.interactiveIO((Integer) flags.get("eof"))), clazz);

        try {
            BrainfuckCompiler.compile(prog, compiler);
            byte[] dump = compiler.dump();
            try {
                Files.write(Paths.get(targetFile), dump, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                throw new BrainfuckCompilationException("Error writing file", e);
            }

            System.out.println("Successful compilation.");
        } catch (BrainfuckPointedCompilationException e) {
            System.out.println("Invalid syntax: " + e.getError());
            System.out.println(e.getPointedError());
        } catch (BrainfuckCompilationException e) {
            System.out.println("Failed compilation:");
            e.printStackTrace();
        }
    }

}
