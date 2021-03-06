/*
 * Copyright 2013 Krispijn A. Scholte
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package monstar;

/**
 * This is a utility class that has miscellaneous functions used throughout 
 * the program.
 * 
 * @author Krispijn Scholte
 */
class MonstarUtility {
    
    public static String getClassString (Integer classID){
        // Returns a string representation of a shiptype id. For more info on this
        // please see thesis Section 6-2-2.
        String theClass = "cNonTrending";
        switch(classID){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
                theClass = "cReserved";
                break;
                
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
                theClass = "cWingInGround"; 
                break;
                
            case 30:
                theClass = "cFishing";
                break;
                        
            case 31:
            case 32:                
            case 33:
            case 34:
            case 35:
                theClass = "cNonTrending";
                break;
                
            case 36:
                theClass = "cSailing";
                break;
                
            case 37:
                theClass = "cPleasure";
                break;
                
            case 38:
            case 39:
                theClass = "cReserved";
                break;

            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 49:
                theClass = "cHighspeed";
                break;
                
            case 50:
            case 51:
            case 52:
            case 53:
            case 54:
            case 55:
                theClass = "cNonTrending";
                break;
                
            case 56:
            case 57:
                theClass = "cReserved";
                break;
                
            case 58:
            case 59:
                theClass = "cNonTrending";
                break;
                
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 68:
            case 69:
                theClass = "cPassenger";
                break;
                
            case 70:
            case 71:
            case 72:
            case 73:
            case 74:
            case 75:
            case 76:
            case 77:
            case 78:
            case 79:
                theClass = "cCargo";
                break;
                
            case 80:
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
                theClass = "cTanker";
                break;
                
            case 90:
            case 91:
            case 92:
            case 93:
            case 94:
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
                theClass = "cOther";
                break;
        }       
        
        return theClass;
    }
}
