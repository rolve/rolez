package rolez.annotation.processing;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

public class Message {
	Kind kind;
	String message;
	Element element;
	
	public Message(Kind kind, String message) {
		this.kind = kind;
		this.message = message;
	}
	
	public Message(Kind kind, String message, Element element) {
		this.kind = kind;
		this.message = message;
		this.element = element;
	}

	public void setElement(Element element) {
		this.element = element;
	}
	
	public void print(Messager messager) {
		if (element != null) {
			messager.printMessage(this.kind, this.message, this.element);
		} else {
			messager.printMessage(this.kind, this.message);
		}
	}
}
