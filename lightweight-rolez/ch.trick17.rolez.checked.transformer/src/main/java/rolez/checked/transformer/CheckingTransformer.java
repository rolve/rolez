package rolez.checked.transformer;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import soot.Body;
import soot.BodyTransformer;

public class CheckingTransformer extends BodyTransformer {
	
	static final Logger logger = LogManager.getLogger(CheckingTransformer.class);
	
	@Override
	protected void internalTransform(Body b, String phaseName, Map options) {
		logger.debug("Transforming " + b.getMethod().getDeclaringClass() + ":" + b.getMethod().getSignature());
	}
}
