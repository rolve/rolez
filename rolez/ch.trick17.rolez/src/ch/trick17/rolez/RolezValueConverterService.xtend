package ch.trick17.rolez

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
                "'" + toString.convertToJavaString + "'"
            }
            override toValue(String it, INode _)  {
                substring(1, length-1).convertFromJavaString(true).charAt(0)
            }
        }
    }
    
    @ValueConverter(rule = "LONG")
    def IValueConverter<Long> LONG() {
        new IValueConverter<Long> {
            override toString(Long it) {
                it + "L"
            }
            override toValue(String it, INode _)  {
                Long.valueOf(replaceAll("\\s", "").substring(0, length-1))
            }
        }
    }
}