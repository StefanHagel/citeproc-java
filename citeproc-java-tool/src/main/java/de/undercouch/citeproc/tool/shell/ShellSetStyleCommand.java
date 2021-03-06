// Copyright 2014 Michel Kraemer
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package de.undercouch.citeproc.tool.shell;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.helper.tool.ToolUtils;
import de.undercouch.citeproc.tool.AbstractCSLToolCommand;
import de.undercouch.underline.InputReader;
import de.undercouch.underline.OptionParserException;
import de.undercouch.underline.UnknownAttributes;
import jline.console.completer.Completer;

/**
 * Set the current citation style
 * @author Michel Kraemer
 */
public class ShellSetStyleCommand extends AbstractCSLToolCommand implements Completer {
	/**
	 * The current styles
	 */
	private List<String> styles;
	
	@Override
	public String getUsageName() {
		return "set style";
	}
	
	@Override
	public String getUsageDescription() {
		return "Set the current citation style";
	}
	
	/**
	 * Sets the current styles
	 * @param styles the styles
	 */
	@UnknownAttributes("STYLE")
	public void setStyles(List<String> styles) {
		this.styles = styles;
	}
	
	@Override
	public boolean checkArguments() {
		if (styles == null || styles.isEmpty()) {
			error("no style specified");
			return false;
		}
		if (styles.size() > 1) {
			error("you can only specify one style");
			return false;
		}
		
		String s = styles.get(0);
		try {
			Set<String> supportedStyles = CSL.getSupportedStyles();
			if (!supportedStyles.contains(s)) {
				String message = "unsupported citation style `" + s + "'";
				String dyms = ToolUtils.getDidYouMeanString(supportedStyles, s);
				if (dyms != null && !dyms.isEmpty()) {
					message += "\n\n" + dyms;
				}
				error(message);
				return false;
			}
		} catch (IOException e) {
			//could not check supported styles. ignore
		}
		
		return true;
	}
	
	@Override
	public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
			throws OptionParserException, IOException {
		ShellContext.current().setStyle(styles.get(0));
		return 0;
	}
	
	@Override
	public int complete(String buffer, int cursor, List<CharSequence> candidates) {
		Set<String> sf;
		try {
			sf = CSL.getSupportedStyles();
		} catch (IOException e) {
			//could not get list of supported styles. ignore.
			return 0;
		}
		
		if (buffer.trim().isEmpty()) {
			candidates.addAll(sf);
		} else {
			String[] args = buffer.split("\\s+");
			String last = args[args.length - 1];
			for (String f : sf) {
				if (f.startsWith(last)) {
					candidates.add(f);
				}
			}
		}
		return 0;
	}
}
