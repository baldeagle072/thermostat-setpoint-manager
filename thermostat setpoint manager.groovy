/**
 *  Thermostat Setpoint Manager 1.0
 *
 *  Version Date: 4/15/2015
 *
 *  Copyright 2015 Eric Roberts
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Thermostat Setpoint Manager 1.0",
    namespace: "baldeagle072",
    author: "Eric Roberts",
    description: "Changes the thermostat setpoints based on mode.",
    category: "Green Living",
    iconUrl: "http://baldeagle072.github.io/icons/thermostat@1x.png",
    iconX2Url: "http://baldeagle072.github.io/icons/thermostat@2x.png",
    iconX3Url: "http://baldeagle072.github.io/icons/thermostat@3x.png")


preferences {
    page name: "numSetpointsSetup"
    page name: "setpointsSetup"
}

def numSetpointsSetup() {

    def allModes = location.modes
    
    def pageProperties = [
        name:           "numSetpointsSetup",
        title:          "Number of Setpoint groups",
        nextPage:       "setpointsSetup",
        uninstall:      true
    ]
    
    return dynamicPage(pageProperties) {
        section("Which thermostat(s) do you want to set?") {
            input "tstat", "capability.thermostat", title: "Thermostat", required: true, multiple: true
        }
        
        section("How many setpoint groups?") {
            paragraph "You can group several modes into a single setpoint. You will be able to set a heating and cooling setpoint for each group."
            input "numSetpoints", "number", title: "Number of groups"
            paragraph "Your current modes are: $allModes"
        }
        
        section([mobileOnly:true]) {
            label title: "Assign a name", required: false
        }
    }
}

def setpointsSetup() {
    
    def pageProperties = [
        name:           "setpointsSetup",
        title:          "Configure Setpoint Groups",
        install:        true,
        uninstall:      true
    ]
    
    return dynamicPage(pageProperties) {
        for (int n = 1; n <= numSetpoints; n++) {
            section("Setpoint Group ${n}", hideable:true, hidden:true) {
                paragraph "Select a heating setpoint, cooling setpoint and the modes to set in. Don't repeat any mode!"
                input "s${n}_modes", "mode", title: "Modes to set in", required: true, multiple: true
                input "s${n}_heatingSetpoint", "number", title: "Heating Setpoint", required: false
                input "s${n}_coolingSetpoint", "number", title: "Cooling Setpoint", required: false
            }
        }
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"

    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    unsubscribe()
    initialize()
}

def initialize() {
    def coolingMap = [:]
    def heatingMap = [:]
    
    for (int n = 1; n <= numSetpoints; n++) {
        def modes = settings."s${n}_modes"
        def heatingSetpoint = settings."s${n}_heatingSetpoint"
        def coolingSetpoint = settings."s${n}_coolingSetpoint"
        
        log.debug "modes: $modes, heatingSetpoint: $heatingSetpoint, coolingSetpoint: $coolingSetpoint"
        
        modes.each {
            if (coolingSetpoint) {
                coolingMap["$it"] = coolingSetpoint
            }
            
            if (heatingSetpoint) {
                heatingMap["$it"] = heatingSetpoint
            }
        }
    }
    
    log.debug "coolingMap: $coolingMap, heatingMap: $heatingMap"
    
    state.coolingMap = coolingMap
    state.heatingMap = heatingMap
    
    subscribe(location, onLocation)
}

def onLocation(evt) {
    def currentMode = evt.value
    
    def newCoolingSetpoint = state.coolingMap["$currentMode"]
    def newHeatingSetpoint = state.heatingMap["$currentMode"]
    
    log.debug "currentMode: $currentMode, newCoolingSetpoint: $newCoolingSetpoint, newHeatingSetpoint: $newHeatingSetpoint"
    
    if (newCoolingSetpoint) {
        tstat.setCoolingSetpoint(newCoolingSetpoint)
    }
    
    if (newHeatingSetpoint) {
        tstat.setHeatingSetpoint(newHeatingSetpoint)
    }
}




