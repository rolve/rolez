package ch.trick17.rolez

import org.apache.log4j.Logger

class Config {
    
    static val noTaskParam = "noTaskParam"
    static val noRoleAnalysis = "noRoleAnalysis"
    static val noChildTasksAnalysis = "noChildTasksAnalysis"
    
    static extension val Logger = Logger.getLogger(Config)
    
    new() {
        if(!taskParamEnabled)
            warn("Task parameter generation disabled")
        if(!roleAnalysisEnabled)
            warn("Role analysis disabled")
        if(!childTasksAnalysisEnabled)
            warn("Child tasks analysis disabled")
    }
    
    def taskParamEnabled() {
        System.getProperty(noTaskParam) == null
    }
    
    def roleAnalysisEnabled() {
        System.getProperty(noRoleAnalysis) == null
    }
    
    def childTasksAnalysisEnabled() {
        System.getProperty(noChildTasksAnalysis) == null
    }
}