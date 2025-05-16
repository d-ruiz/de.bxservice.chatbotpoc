/**********************************************************************
 * This file is part of iDempiere ERP Open Source                      *
 * http://www.idempiere.org                                            *
 *                                                                     *
 * Copyright (C) Contributors                                          *
 *                                                                     *
 * This program is free software; you can redistribute it and/or       *
 * modify it under the terms of the GNU General Public License         *
 * as published by the Free Software Foundation; either version 2      *
 * of the License, or (at your option) any later version.              *
 *                                                                     *
 * This program is distributed in the hope that it will be useful,     *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of      *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the        *
 * GNU General Public License for more details.                        *
 *                                                                     *
 * You should have received a copy of the GNU General Public License   *
 * along with this program; if not, write to the Free Software         *
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,          *
 * MA 02110-1301, USA.                                                 *
 *                                                                     *
 * Contributors:                                                       *
 * - Diego Ruiz  - BX Service GmbH                                     *
 **********************************************************************/
package de.bxservice.chatbotpoc.process;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.compiere.util.Util;

public class ChatbotUtils {

	public static String getPythonResponse(String question) {

		if (Util.isEmpty(question)) {
			return "@Error@: Question cannot be empty";
		}

		try {
			// The question you want to ask Python
			String pythonPath = "/home/diego/tmp/lc-env/bin/python";

			// Build the command to run the Python script
			ProcessBuilder processBuilder = new ProcessBuilder(pythonPath, "/home/diego/tmp/pocSQLv3.py", question);
			processBuilder.redirectErrorStream(true);

			// Start the process
			Process process = processBuilder.start();

			// Get the output of the script
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			StringBuilder output = new StringBuilder();

			// Read the output line by line
			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}

			// Wait for the script to finish execution
			process.waitFor();

			return output.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "Error";
	}

}
