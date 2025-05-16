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
package de.bxservice.chatbotpoc.webui;

import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.Textbox;
import org.adempiere.webui.panel.ADForm;
import org.adempiere.webui.panel.CustomForm;
import org.adempiere.webui.panel.IFormController;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Div;
import org.zkoss.zul.Html;
import org.zkoss.zul.Vlayout;

import de.bxservice.chatbotpoc.process.ChatbotUtils;

public class ChatBotForm implements IFormController,  org.zkoss.zk.ui.event.EventListener<Event> {
	
	private CustomForm chatForm = new CustomForm();
    private Textbox inputTextbox;
    private Html responseHtml;

    public ChatBotForm() {
    	Vlayout vLayout = new Vlayout();
    	vLayout.setVflex("1");
    	vLayout.setHflex("1");

    	// Input area at the top
        Div inputArea = new Div();
        inputArea.setStyle("display: flex; align-items: center; margin-bottom: 10px;");

        inputTextbox = new Textbox();
        inputTextbox.setHflex("1");
        inputTextbox.setPlaceholder("Type your message...");
        inputArea.appendChild(inputTextbox);

        Button sendButton = new Button("Send");
        sendButton.setStyle("margin-left: 5px;");
        sendButton.addEventListener(Events.ON_CLICK, this);
        inputArea.appendChild(sendButton);

        vLayout.appendChild(inputArea);

        // Message display area below the input
        responseHtml = new Html();
        responseHtml.setHflex("1");
        responseHtml.setVflex("1");
        responseHtml.setStyle("overflow-y: auto; border: 1px solid #ccc; padding: 5px;");

        Div htmlWrapper = new Div();
        htmlWrapper.setVflex("1");
        htmlWrapper.setHflex("1");
        htmlWrapper.appendChild(responseHtml);
        vLayout.appendChild(htmlWrapper);
        
        
        chatForm.appendChild(vLayout);
    }

    private void sendMessage() {
    	String message = inputTextbox.getValue();
        if (message != null && !message.trim().isEmpty()) {
            // Append the user's message to the responseTextbox
            String currentContent = responseHtml.getContent();
            responseHtml.setContent(currentContent + 
                    "<p style='margin-bottom:2px;'><b>You:</b> " + escapeHtml(message) + "</p>");

                // Clear the input box
                inputTextbox.setValue("");

                // Simulate a bot response and append it to the responseHtml, aligned to the left
                String botResponse = generateBotResponse(message);
                responseHtml.setContent(responseHtml.getContent() + 
                    "<p style='margin-bottom:20px; background-color: #F2F2F2;'><b>Bot:</b> " + botResponse + "</p>");
        }
    }

    private String generateBotResponse(String userMessage) {
        return ChatbotUtils.getPythonResponse(userMessage);
    }
    
    private String escapeHtml(String input) {
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
    
	@Override
	public ADForm getForm() {
		return chatForm;
	}

	@Override
	public void onEvent(Event event) throws Exception {
        sendMessage();		
	}	

}
