package com.talosvfx.talos.editor.utils;

import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamingUtils {



	public static String getNewName (String newNameAttempt, Supplier<Collection<String>> potentialConflicts) {
		Pattern extractBase = Pattern.compile(  "(.*)\\([0-9]*\\)$");

		Collection<String> potentialConflictStrings = potentialConflicts.get();

		if (!potentialConflictStrings.contains(newNameAttempt)) {
			return newNameAttempt;
		}

		Matcher matcher = extractBase.matcher(newNameAttempt);
		if (matcher.matches()) {
			newNameAttempt = matcher.group(1);
		}
		Pattern duplicatePattern = Pattern.compile(newNameAttempt + "\\([0-9]*\\)$");

		Predicate<String> predicate = duplicatePattern.asPredicate();
		long count = potentialConflictStrings.stream().filter(predicate).count();

		if (count > 0) {
			//We have things that match our regex

			//Something already matches it, lets test new names

			int i = 1;
			while (true) {
				if (i > 10000) {
					throw new GdxRuntimeException("Ok you're being silly now");
				}

				String test = newNameAttempt + "(" + (i) + ")";

				i++;


				if (potentialConflictStrings.contains(test)) {
					continue;
				} else {
					return test;
				}


			}

		} else {
			//First one
			return newNameAttempt+"(" + 1 + ")";
		}
	}

//	public static void main (String[] args) {
//		Supplier<Collection<String>> supplier = new Supplier<Collection<String>>() {
//			@Override
//			public Collection<String> get () {
//				ArrayList<String> test = new ArrayList<>();
//				test.add("banana");
//				test.add("banana(2)");
//				test.add("banana(1)");
//				test.add("banana(3)");
//				test.add("banana2");
//				test.add("banana3");
//				return test;
//			}
//		};
//		System.out.println(getNewName("banana", supplier));
//		System.out.println(getNewName("banana1", supplier));
//		System.out.println(getNewName("banana2", supplier));
//		System.out.println(getNewName("banana3", supplier));
//	}

}
