/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

/*
 * This file is part of the Echo Web Application Framework (hereinafter "Echo").
 * Copyright (C) 2002-2009 NextApp, Inc.
 *
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
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
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 */

//_________________________
// Object EchoTextComponent

/**
 * Object/namespace for Text Component support.
 * This object/namespace should not be used externally.
 */
EchoTextComponent = Core.extend(
        {

            $static: {

                /**
                 * Returns the TextComponent data object instance based on the root element id
                 * of the TextComponent.
                 *
                 * @param element the root element or element id of the TextComponent
                 * @return the relevant TextComponent instance
                 */
                getComponent: function (element) {
                    return EchoDomPropertyStore.getPropertyValue(element, "component");
                },

                /**
                 * Processes a focus blur event:
                 * Records the current state of the text field to the ClientMessage.
                 * Delegates to data object method.
                 *
                 * @param echoEvent the event, preprocessed by the
                 *        <code>EchoEventProcessor</code>
                 */
                processBlur: function (echoEvent) {
                    var textComponent = EchoTextComponent.getComponent(echoEvent.registeredTarget);
                    textComponent.processBlur(echoEvent);
                },

                /**
                 * Processes a text change event:
                 * Records the current state of the text field to the ClientMessage.
                 * Delegates to data object method.
                 *
                 * @param echoEvent the event, preprocessed by the
                 *        <code>EchoEventProcessor</code>
                 */
                processChange: function (echoEvent) {
                    var textComponent = EchoTextComponent.getComponent(echoEvent.registeredTarget);
                    textComponent.processChange(echoEvent);
                },

                /**
                 * Processes a focus event:
                 * Notes focus state in ClientMessage.
                 * Delegates to data object method.
                 *
                 * @param echoEvent the event, preprocessed by the
                 *        <code>EchoEventProcessor</code>
                 */
                processFocus: function (echoEvent) {
                    var textComponent = EchoTextComponent.getComponent(echoEvent.registeredTarget);
                    textComponent.processFocus(echoEvent);
                },

                /**
                 * Processes a key press event:
                 * Initiates an action in the event that the key pressed was the
                 * ENTER key.
                 * Delegates to data object method.
                 *
                 * @param e the DOM Level 2 event, if available
                 */
                processKeyPress: function (e) {
                    e = e ? e : window.event;
                    var target = EchoDomUtil.getEventTarget(e);
                    var textComponent = EchoTextComponent.getComponent(target);
                    textComponent.processKeyPress(e);
                },

                /**
                 * Processes a key up event:
                 * Records the current state of the text field to the ClientMessage.
                 * Delegates to data object method.
                 *
                 * @param echoEvent the event, preprocessed by the
                 *        <code>EchoEventProcessor</code>
                 */
                processKeyUp: function (echoEvent) {
                    var textComponent = EchoTextComponent.getComponent(echoEvent.registeredTarget);
                    textComponent.processKeyUp(echoEvent);
                }
            },

            /**
             * Creates a new text component support object.
             *
             * @param elementId the id of the supported text component element
             */
            $construct: function (elementId) {
                this.elementId = elementId;
                this.hash = 0;
            },

            /**
             * Disposes of resources used by the support object.
             */
            dispose: function () {
                var element = this.getElement();
                EchoEventProcessor.removeHandler(element, "mouseout");
                EchoEventProcessor.removeHandler(element, "blur");
                EchoEventProcessor.removeHandler(element, "focus");
                EchoEventProcessor.removeHandler(element, "keyup");
                EchoDomUtil.removeEventListener(element, "keypress", EchoTextComponent.processKeyPress, false);

                // Remove any updates to text component that occurred during client/server transaction.
                EchoClientMessage.removePropertyElement(element.id, "text");

                EchoDomPropertyStore.dispose(element);
            },

            /**
             * Processes a user "action request" on the text component i.e., the pressing
             * of the ENTER key when the the component is focused.
             * If any server-side <code>ActionListener</code>s are registered, an action
             * will be set in the ClientMessage and a client-server connection initiated.
             */
            doAction: function () {
                if (!this.serverNotify) {
                    return;
                }

                if (!this.enabled || !EchoClientEngine.verifyInput(this.getElement(), false)) {
                    // Don't process actions when client/server transaction in progress.
                    return;
                }

                this.updateClientMessage();
                EchoClientMessage.setActionValue(this.elementId, "action");
                EchoServerTransaction.connect();
            },

            /**
             * Returns the text component's DOM element.
             */
            getElement: function () {
                return document.getElementById(this.elementId);
            },

            $virtual: {

                /**
                 * Initializes the text component support object.
                 */
                init: function () {
                    var element = this.getElement();

                    if (!this.enabled) {
                        element.readOnly = true;
                    }

                    if (this.text) {
                        this.setText(this.text);
                    } else {
                        this.hash = this.hashCode(element.value);
                    }

                    if (this.cursorPosition !== 0) {
                        this.setCursorPosition(element, this.cursorPosition);
                    }

                    if (this.horizontalScroll !== 0) {
                        element.scrollLeft = this.horizontalScroll;
                    }

                    this.multipleLines = element.nodeName.toLowerCase() != "input";

                    if (this.verticalScroll !== 0) {
                        if (EchoClientProperties.get("quirkIERepaint")) {
                            // Avoid IE quirk where browser will fail to set scroll bar position.
                            var originalWidth = element.style.width;
                            var temporaryWidth = parseInt(element.clientWidth, 10) - 1;
                            element.style.width = temporaryWidth + "px";
                            element.style.width = originalWidth;
                        }
                        element.scrollTop = this.verticalScroll;
                    }

                    if (EchoClientProperties.get("quirkMozillaTextInputRepaint")) {
                        // Avoid Mozilla quirk where text will be rendered outside of text field
                        // (this appears to be a Mozilla bug).
                        var noValue = !element.value;
                        if (noValue) {
                            element.value = "-";
                        }
                        var currentWidth = element.style.width;
                        element.style.width = "20px";
                        element.style.width = currentWidth;
                        if (noValue) {
                            element.value = "";
                        }
                    }

                    EchoEventProcessor.addHandler(element, "mouseout", "EchoTextComponent.processChange");
                    EchoEventProcessor.addHandler(element, "blur", "EchoTextComponent.processBlur");
                    EchoEventProcessor.addHandler(element, "focus", "EchoTextComponent.processFocus");
                    EchoEventProcessor.addHandler(element, "keyup", "EchoTextComponent.processKeyUp");

                    EchoDomUtil.addEventListener(element, "keypress", EchoTextComponent.processKeyPress, false);

                    EchoDomPropertyStore.setPropertyValue(element, "component", this);
                },

                setText: function (text) {
                    var element = this.getElement();
                    this.hash = this.hashCode(text);
                    element.value = text;
                },

                /**
                 * Processes a focus blur event:
                 * Records the current state of the text field to the ClientMessage.
                 *
                 * @param echoEvent the event, preprocessed by the
                 *        <code>EchoEventProcessor</code>
                 */
                processBlur: function (echoEvent) {
                    if (!this.enabled || !EchoClientEngine.verifyInput(this.getElement())) {
                        return;
                    }

                    this.updateClientMessage();
                    EchoFocusManager.setFocusedState(this.elementId, false);
                },

                /**
                 * Processes a text change event.
                 * Records the current state of the text field to the ClientMessage.
                 *
                 * @param echoEvent the event, preprocessed by the
                 *        <code>EchoEventProcessor</code>
                 */
                processChange: function (echoEvent) {
                    if (!this.enabled || !EchoClientEngine.verifyInput(this.getElement())) {
                        return;
                    }
                    this.updateClientMessage();
                },

                /**
                 * Processes a focus event:
                 * Notes focus state in ClientMessage.
                 *
                 * @param echoEvent the event, preprocessed by the
                 *        <code>EchoEventProcessor</code>
                 */
                processFocus: function (echoEvent) {
                    if (!this.enabled || !EchoClientEngine.verifyInput(this.getElement())) {
                        return;
                    }

                    EchoFocusManager.setFocusedState(this.elementId, true);
                    this.updateClientMessage(); // update cursor position
                },

                /**
                 * Processes a key press event:
                 * Initiates an action in the event that the key pressed was the
                 * ENTER key.
                 *
                 * @param e the DOM Level 2 event
                 */
                processKeyPress: function (e) {
                    if (!this.enabled || !EchoClientEngine.verifyInput(this.getElement(), true)) {
                        EchoDomUtil.preventEventDefault(e);
                        return;
                    }
                    if (e.keyCode == 13) {
                        if (!this.multipleLines) {
                            EchoDomUtil.preventEventDefault(e);
                        }
                        this.doAction();
                    }
                },

                /**
                 * Processes a key up event:
                 * Records the current state of the text field to the ClientMessage.
                 *
                 * @param echoEvent the event, preprocessed by the
                 *        <code>EchoEventProcessor</code>
                 */
                processKeyUp: function (echoEvent) {
                    var element = this.getElement();
                    if (!this.enabled || !EchoClientEngine.verifyInput(element, true)) {
                        EchoDomUtil.preventEventDefault(echoEvent);
                        return;
                    }

                    if (this.maximumLength >= 0) {
                        if (element.value && element.value.length > this.maximumLength) {
                            element.value = element.value.substring(0, this.maximumLength);
                        }
                    }

                    this.updateClientMessage();
                },

                /**
                 * Updates the component state in the outgoing <code>ClientMessage</code>.
                 */
                updateClientMessage: function () {
                    var element = this.getElement();
                    var newHash = this.hashCode(element.value);
                    this.cursorPosition = this.getCursorPosition(element);

                    if (this.hash != newHash) {
                        var textPropertyElement = EchoClientMessage.createPropertyElement(this.elementId, "text");

                        if (textPropertyElement.firstChild) {
                            textPropertyElement.firstChild.nodeValue = element.value;
                        } else {
                            textPropertyElement.appendChild(EchoClientMessage.messageDocument.createTextNode(element.value));
                        }
                        this.hash = newHash;
                    }

                    EchoClientMessage.setPropertyValue(this.elementId, "horizontalScroll", element.scrollLeft);
                    EchoClientMessage.setPropertyValue(this.elementId, "verticalScroll", element.scrollTop);
                    EchoClientMessage.setPropertyValue(this.elementId, "cursorPosition", this.cursorPosition);
                }
            },

            hashCode: function (text) {
                var hash = 0;
                if (text) {
                    for (i = 0; i < text.length; i++) {
                        var ch = text.charCodeAt(i);
                        hash = 31 * hash + ch;
                        hash = hash & hash; // convert to 32 bit integer
                    }
                }
                return hash;
            },

            // From http://stackoverflow.com/questions/235411/is-there-an-internet-explorer-approved-substitute-for-selectionstart-and-selecti
            getCursorPosition: function (element) {
                var result = 0;
                if (typeof element.selectionEnd == "number") {
                    result = element.selectionEnd;
                } else {
                    var range = document.selection.createRange();
                    if (range && range.parentElement() == element) {
                        var len = element.value.length;
                        var normalizedValue = element.value.replace(/\r\n/g, "\n");
                        var textInputRange = element.createTextRange();
                        textInputRange.moveToBookmark(range.getBookmark());
                        var endRange = element.createTextRange();
                        endRange.collapse(false);
                        if (textInputRange.compareEndPoints("StartToEnd", endRange) > -1) {
                            result = len;
                        }
                        else {
                            result = -textInputRange.moveStart("character", -len);
                            result += normalizedValue.slice(0, result).split("\n").length - 1;
                        }
                    }
                }
                return result;
            },

            // From http://stackoverflow.com/questions/512528/set-cursor-position-in-html-textbox
            setCursorPosition: function (element, cursorPos) {
                element.value = element.value;
                // ^ this is used to not only get "focus", but
                // to make sure we don't have it everything -selected-
                // (it causes an issue in chrome, and having it doesn't hurt any other browser)

                if (element.createTextRange) {
                    // IE
                    var range = element.createTextRange();
                    range.move('character', cursorPos);
                    range.select();
                    return true;
                }
                else {
                    if (element.selectionStart || element.selectionStart === 0) {
                        element.focus();
                        element.setSelectionRange(cursorPos, cursorPos);
                        return true;
                    }
                    else {
                        element.focus();
                        return false;
                    }
                }
            }
        });

/**
 * Static object/namespace for Text Component MessageProcessor
 * implementation.
 */
EchoTextComponent.MessageProcessor = {

    /**
     * MessageProcessor process() implementation
     * (invoked by ServerMessage processor).
     *
     * @param messagePartElement the <code>message-part</code> element to process.
     */
    process: function (messagePartElement) {
        for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
            if (messagePartElement.childNodes[i].nodeType == 1) {
                switch (messagePartElement.childNodes[i].tagName) {
                    case "init":
                        EchoTextComponent.MessageProcessor.processInit(messagePartElement.childNodes[i]);
                        break;
                    case "dispose":
                        EchoTextComponent.MessageProcessor.processDispose(messagePartElement.childNodes[i]);
                        break;
                    case "set-text":
                        EchoTextComponent.MessageProcessor.processSetText(messagePartElement.childNodes[i]);
                        break;
                }
            }
        }
    },

    /**
     * Processes a <code>dispose</code> message to finalize the state of a
     * Text Component that is being removed.
     *
     * @param disposeMessageElement the <code>dispose</code> element to process
     */
    processDispose: function (disposeMessageElement) {
        for (var item = disposeMessageElement.firstChild; item; item = item.nextSibling) {
            var elementId = item.getAttribute("eid");
            var textComponent = EchoTextComponent.getComponent(elementId);
            if (textComponent) {
                textComponent.dispose();
            }
        }
    },

    /**
     * Processes a <code>set-text</code> message to update the text displayed in a
     * Text Component.
     *
     * @param setTextMessageElement the <code>set-text</code> element to process
     */
    processSetText: function (setTextMessageElement) {
        for (var item = setTextMessageElement.firstChild; item; item = item.nextSibling) {
            var elementId = item.getAttribute("eid");
            var text = item.getAttribute("text");
            var textComponent = EchoTextComponent.getComponent(elementId);
            textComponent.setText(text);

            if (textComponent.cursorPosition !== 0) {
                var element = document.getElementById(elementId);
                textComponent.setCursorPosition(element, textComponent.cursorPosition);
            }

            // Remove any updates to text component that occurred during client/server transaction.
            EchoClientMessage.removePropertyElement(textComponent.id, "text");
        }
    },

    /**
     * Processes an <code>init</code> message to initialize the state of a
     * Text Component that is being added.
     *
     * @param initMessageElement the <code>init</code> element to process
     */
    processInit: function (initMessageElement) {
        for (var item = initMessageElement.firstChild; item; item = item.nextSibling) {
            var elementId = item.getAttribute("eid");

            var textComponent = new EchoTextComponent(elementId);
            textComponent.enabled = item.getAttribute("enabled") != "false";
            textComponent.text = item.getAttribute("text") ? item.getAttribute("text") : null;
            textComponent.serverNotify = item.getAttribute("server-notify") == "true";
            textComponent.maximumLength = item.getAttribute("maximum-length") ? item.getAttribute("maximum-length") : -1;
            textComponent.horizontalScroll = item.getAttribute("horizontal-scroll") ?
                                             parseInt(item.getAttribute("horizontal-scroll"), 10) : 0;
            textComponent.verticalScroll = item.getAttribute("vertical-scroll") ?
                                           parseInt(item.getAttribute("vertical-scroll"), 10) : 0;
            textComponent.cursorPosition = item.getAttribute("cursor-position") ?
                                           parseInt(item.getAttribute("cursor-position"), 10) : 0;

            textComponent.init();
        }
    }
};
