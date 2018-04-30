package ch.trick17.rolez

import org.apache.log4j.Logger

class Config {
    
    static val noRoleAnalysis = "noRoleAnalysis"
    static val noChildTasksAnalysis = "noChildTasksAnalysis"
    
    static extension val Logger = Logger.getLogger(Config)
    
    new() {
        if(!roleAnalysisEnabled)
            warn("Role analysis disabled")
        if(!childTasksAnalysisEnabled)
            warn("Child tasks analysis disabled")
    }
    
    def roleAnalysisEnabled() {
        System.getProperty(noRoleAnalysis) == null
    }
    
    def childTasksAnalysisEnabled() {
        System.getProperty(noChildTasksAnalysis) == null
    }
}