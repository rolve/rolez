package ch.trick17.rolez.lang

import org.eclipse.xtext.common.services.DefaultTerminalConverters
import org.eclipse.xtext.conversion.IValueConverter
import org.eclipse.xtext.conversion.ValueConverter
import org.eclipse.xtext.nodemodel.INode

import static extension org.eclipse.xtext.util.Strings.*

class RolezValueConverterService extends DefaultTerminalConverters {
    
    @ValueConverter(rule = "CHAR")
    def IValueConverter<Character> CHAR() {
        new IValueConverter<Character> {
            override toString(Character it) {
                "'" + toString.convertToJavaString.replace("\\\"", "\"").replace("'", "\\'") + "'"
            }
            override toValue(String it, INode _)  {
                substring(1, length-1).replace("\"", "\\\"").replace("\\'", "'")
                    .convertFromJavaString(true).charAt(0)
            }
        }
    }
}