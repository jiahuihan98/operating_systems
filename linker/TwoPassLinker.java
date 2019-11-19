//Jiahui Han, jh5226, N14900657
//Operating System, Lab1

import java.util.*;

public class TwoPassLinker {
    public static void main(String[] args) {
        //store input
        Scanner sc = new Scanner(System.in);
        int numModule = sc.nextInt();
        int symbolLimit = 6;

        ArrayList<String> usageSymbol = new ArrayList<>(); //store lines of usage symbol
        ArrayList<Integer> usageAddress = new ArrayList<>(); //store lines of usage address ABSOLUTE

        ArrayList<Integer> initialAddress = new ArrayList<>(); //read in addresses
        ArrayList<Integer> baseAddress = new ArrayList<>();//store base address for each module
        HashMap<String, Integer> symbolMapFinal = new HashMap<>();// to store addresses for each symbol after error checking

        //PASS 1
        int countInstruction = 0;
        for (int i = 0; i < numModule; i++) {
            HashMap<String, Integer> symbolMap = new HashMap<>(); //to store addresses for each symbol before error checking
            //go through definitions
            int numDefinition = sc.nextInt();
            for (int j = 0; j < numDefinition; j++) {
                String nameSymbol = sc.next();

                //if name of the symbol is too long
                if (nameSymbol.length() > symbolLimit) {
                    nameSymbol = nameSymbol.substring(0, symbolLimit);
                }

                int address = sc.nextInt();
                //check if any definition is multiplied defined
                if (symbolMapFinal.containsKey(nameSymbol)) {
                    System.out.println("Error: " + nameSymbol + " is multiply defined; first value used.");
                }
                else {
                    symbolMap.put(nameSymbol, address);
                }

            }

            //go through usages
            int numUsage = sc.nextInt();
            for (int j = 0; j < numUsage; j++) {
                String nameSymbol = sc.next();
                int address = sc.nextInt() + countInstruction;

                //if use the same instruction, which means they are at the same abs address
                if (usageAddress.contains(address)) {
                    System.out.println("Error: multiple symbols are listed as used in the same instruction, last one used");
                    int curInd = usageAddress.indexOf(address);
                    usageSymbol.remove(curInd);
                    usageAddress.remove(curInd);
                }

                usageSymbol.add(nameSymbol);
                usageAddress.add(address);
            }

            baseAddress.add(countInstruction);

            //go through hashmap to check if any address exceed current module size
            int numInstruction = sc.nextInt();
            for (Map.Entry<String, Integer> entry : symbolMap.entrySet()) {
                if (entry.getValue() >= numInstruction) {
                    System.out.println("\nError: The definition of " + entry.getKey() + " is outside module " + i + " zero (relative) used");
                    symbolMapFinal.put(entry.getKey(), countInstruction);
                }
                else {
                    symbolMapFinal.put(entry.getKey(), entry.getValue() + countInstruction);
                }
            }

            for (int j = 0; j < numInstruction; j++) {
                int curInstruction = sc.nextInt();
                int lastDigit = curInstruction % 10;
                if (lastDigit == 2) {
                    if ((curInstruction / 10 % 1000) < 200) {
                        initialAddress.add(curInstruction / 10);
                    }
                    else {
                        //exceed limit
                        System.out.println("\nError: absolute address " + curInstruction + " exceeds the size of the machine, 199 used");
                        initialAddress.add(curInstruction / 10000 * 1000 + 199);
                    }
                }
                else {
                    initialAddress.add(curInstruction);
                }
            }
            countInstruction += numInstruction;
        }

        //PASS 2
        for (int i = 0; i < usageSymbol.size(); i++) {
            String nameSymbol = usageSymbol.get(i);
            //System.out.println(nameSymbol);

            //if this usage is not defined
            if (!(symbolMapFinal.containsKey(nameSymbol))) {
                System.out.println("Error " + nameSymbol + " is not defined; zero used.");
                symbolMapFinal.put(nameSymbol, 0);
            }

            int address = usageAddress.get(i);
            //System.out.println(address);

            //need to find the corresponding module number
            int curModule = 0;
            for (int j = 0; j < baseAddress.size() - 1; j++) {//0, 1, 2,
                if ( (baseAddress.get(j) <= address) && (address < baseAddress.get(j + 1)) ) {
                    curModule = j;
                    break;
                }
            }
            if (address > baseAddress.get(baseAddress.size() - 1)) {
                curModule = baseAddress.size() - 1;
            }
            //System.out.println(curModule);

            if (((initialAddress.get(address)) / 10000) != 0) {

                // check if it is an immediate address
                if ((initialAddress.get(address) % 10) == 1) {
                    System.out.println("Error: Immediate address on use list; treated as External.");
                    int newAddress = initialAddress.get(address) / 10 * 10 + 4;
                    initialAddress.set(address, newAddress);
                }
                //System.out.println("aa");
                if ((initialAddress.get(address) % 10) == 4) {
                    //System.out.println(initialAddress.get(address));
                    int relativeAddress = (initialAddress.get(address) / 10) % 1000;
                    //System.out.println(relativeAddress);
                    while (relativeAddress != 777) {
                        int newAddress = (initialAddress.get(address) / 10000) * 1000 + symbolMapFinal.get(nameSymbol);
                        initialAddress.set(address, newAddress);
                        //System.out.println(curModule + "b");
                        address = relativeAddress + baseAddress.get(curModule);
                        //System.out.println(address + "&");
                        relativeAddress = (initialAddress.get(address) / 10) % 1000;
                    }
                    int newAddress = (initialAddress.get(address) / 10000) * 1000 + symbolMapFinal.get(nameSymbol);
                    initialAddress.set(address, newAddress);
                }
            }
        }//end of pass 2

        //handle last digit cases here
        for (int i = 0; i < initialAddress.size(); i++) {
            if ((initialAddress.get(i) / 10000) != 0) {
                if ((initialAddress.get(i) % 10) == 1) {
                    initialAddress.set(i, initialAddress.get(i) / 10);
                }
                if ((initialAddress.get(i) % 10) == 4) {
                    System.out.println("Error: E type address not on use chain; treated as I type.");
                    initialAddress.set(i, initialAddress.get(i) / 10);
                }
                if ((initialAddress.get(i) % 10) == 3) {
                    //find module
                    int curModule = 0;
                    for (int j = 0; j < baseAddress.size() - 1; j++) {
                        if ( (baseAddress.get(j) <= i) && (i < baseAddress.get(j + 1)) ) {
                            curModule = j;
                            break;
                        }
                        else if (i > baseAddress.get(j + 1)) {
                            curModule = baseAddress.size() - 1;
                        }
                    }
                    //System.out.println(curModule);
                    int temp = initialAddress.get(i) / 10 + baseAddress.get(curModule);
                    initialAddress.set(i, temp);
                }
            }
        }

        //rearrange print format
        System.out.println("Symbol Table");
        for (Map.Entry<String, Integer> entry : symbolMapFinal.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }

        System.out.println("\n\nMemory Map");
        for (int i = 0; i < initialAddress.size(); i++) {
            System.out.println(i + ":  " + initialAddress.get(i));
        }

        //check if any symbol defined but not used
        for (Map.Entry<String, Integer> entry : symbolMapFinal.entrySet()) {
            if (!(usageSymbol.contains(entry.getKey()))) {
                int curModule = 0;
                for (int i = 0; i < baseAddress.size() - 1; i++) {
                    if ( (baseAddress.get(i) <= entry.getValue()) && (entry.getValue() < baseAddress.get(i + 1)) ) {
                        curModule = i;
                        break;
                    }
                    else {
                        curModule = baseAddress.size() - 1;
                    }
                }
                System.out.println("Warning: " + entry.getKey() + " was defined in module " + curModule + " but never used.");
            }
        }

    }
}
