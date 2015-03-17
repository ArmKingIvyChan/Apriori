import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Scanner;

public class AprioriMiner {

    static List<List<String>> transactionList = new ArrayList<List<String>>();
    static List<Rule> ruleList = new ArrayList<Rule>();
    static List<Set<String>> frequentItemList = new ArrayList<Set<String>>();

    public static void main(String[] args) {
        double minSupport = 0, minConfidence = 0;
        File inputDataFile = new File("basket.txt");
        File inputAttrFile = new File("basket-attr.txt");
        System.out.println("\nFor the basket:");
        Scanner scan = new Scanner(System.in);
        System.out.println("Please input the minSupport:");
        minSupport = (double) scan.nextFloat();
        scan.reset();
        System.out.println("Please input the minConfidence:");
        minConfidence = (double) scan.nextFloat();
        scan.reset();
        aprioriAlgorithm(inputDataFile,inputAttrFile,minSupport,minConfidence);
        ruleList.clear();
        File inputDataFile1 = new File("lens.txt");
        File inputAttrFile1 = new File("lens-attr.txt");
        System.out.println("\nFor the lens:");
        System.out.println("Please input the minSupport:");
        minSupport = (double) scan.nextFloat();
        scan.reset();
        System.out.println("Please input the minConfidence:");
        minConfidence = (double) scan.nextFloat();
        scan.reset();
        aprioriAlgorithm(inputDataFile1,inputAttrFile1,minSupport,minConfidence);
    }

    private static ArrayList<Set<String>> getSubsets(ArrayList<String> set)
    {

        ArrayList<Set<String>> subsetCollection = new ArrayList<Set<String>>();

        if (set.size() == 0) {
            subsetCollection.add(new HashSet<String>());
        } else {
            ArrayList<String> reducedSet = new ArrayList<String>();

            reducedSet.addAll(set);

            String first = reducedSet.remove(0);
            ArrayList<Set<String>> subsets = getSubsets(reducedSet);
            subsetCollection.addAll(subsets);

            subsets = getSubsets(reducedSet);

            for (Set<String> subset : subsets) {
                subset.add(first);
            }

            subsetCollection.addAll(subsets);
        }

        return subsetCollection;
    }

    public static <T>Set<T> union(Set<T> setA, Set<T> setB) {
        Set<T> tmp = new HashSet<T>(setA);
        tmp.addAll(setB);
        return tmp;
    }

    public static String[] readAttrNames(File attrNameFile)throws Exception {
        BufferedReader in = new BufferedReader(new FileReader(attrNameFile));
        String attrNamesLine;
        int size=0;
        List<String> attrName1 = new ArrayList<String>();
        while((attrNamesLine = in.readLine()) != null){
            List<String> attrName = new ArrayList<String>();
            attrName.add(attrNamesLine.split("\t")[0].split(" ")[0]);
            attrName1.add(attrName.toString());
        }
        in.close();
        size=attrName1.size();
        String[] attributeNames = new String[size];
        for(int i=0;i<size;i++){
            attributeNames[i] = attrName1.get(i);
        }
        return attributeNames;
    }

    public static void aprioriAlgorithm(File inputDataFile, File inputAttrFile, double minSupport, double minConfidence){
        List<Set<Set<String>>> frequentItemSetList = new ArrayList<Set<Set<String>>>();
        //Getting individual Items and their counts
        Set<String> itemSet = new HashSet<String>();
        Map<String,Integer> itemSupportMap = new HashMap<String,Integer>();
        Map<Set<String>,Integer> frequentItemSupportMap = new HashMap<Set<String>,Integer>();
        ValueComparator bvc =  new ValueComparator(frequentItemSupportMap);
        TreeMap<Set<String>, Integer> sortedSupportMap = new TreeMap<Set<String>, Integer>(bvc);
        try
        {
            //Input the File into transaction data structure
            String[] itemsName = readAttrNames(inputAttrFile);
            BufferedReader br = new BufferedReader(new FileReader(inputDataFile));
            String line;
            while((line = br.readLine()) != null)
            {
                String[] items = line.split(" ");
                List<String> transaction = new ArrayList<String>();
                for(int i=0;i<itemsName.length;i++)
                {
                    if(items[i].equalsIgnoreCase("1")){
                        if(itemSupportMap.containsKey(itemsName[i])){
                            int newCount = (itemSupportMap.get(itemsName[i]))+1;
                            itemSupportMap.remove(itemsName[i]);
                            itemSupportMap.put(itemsName[i],newCount);
                            itemSet.add(itemsName[i]);
                        }
                        else{
                            itemSupportMap.put(itemsName[i],1);
                        }
                        transaction.add(itemsName[i]);
                    }
                }
                transactionList.add(transaction);
            }
            br.close();

            // Starting 6.1 algorithm
            Iterator<Entry<String, Integer>> iterator = itemSupportMap.entrySet().iterator();
            Set<String> F1 = new HashSet<String>();
            while(iterator.hasNext())
            {
                Entry<String, Integer> entry = iterator.next();
                String key = (String)entry.getKey();
                Integer value = (Integer)entry.getValue();
                if((double)(value)/transactionList.size() >= minSupport)
                {
                    F1.add(key);
                }
            }
            Set<Set<String>> setString = new HashSet<Set<String>>();
            setString.add(F1);
            frequentItemSetList.add(setString);

            int k = 1;
            System.out.println("\n1. k = "+ k);
            System.out.println("2. The number of candidate itemsets before pruning: "+ itemsName.length);
            System.out.println("3. The number of candidate itemsets after pruning: "+F1.size());
            System.out.println("4. The candidate itemsets after pruning is:");
            for(int i = 0; i<frequentItemSetList.size(); i++) {
                System.out.println(frequentItemSetList.get(i));
            }
            System.out.println("5. The frequent itemsets is:");
            for(int i = 0; i<frequentItemSetList.size(); i++) {
                System.out.println(frequentItemSetList.get(i));
            }

            while(frequentItemSetList.get(k-1).size() != 0)
            {
                System.out.println("\n1. k = "+(k+1));
                //System.out.println(frequentItemSetList.get(k-1).toString());
                Set<Set<String>> addTofrequentItemSetList = new HashSet<Set<String>>();
                ArrayList<String> entryList = new ArrayList<String>();
                ArrayList<Set<String>> preCkList = new ArrayList<Set<String>>();
                ArrayList<Set<String>> ckList = new ArrayList<Set<String>>();
                Iterator<Set<String>> iterator3 = frequentItemSetList.get(k-1).iterator();
                Scanner scan1 = new Scanner(System.in);
                System.out.println("Input 0 as choosing F(k-1)*F(k-1), input anything else as choosing F(k-1)*F(1)");
                int choice = scan1.nextInt();
                if(choice == 0){
                    //generate candidate itemsets use F(k-1)*F(k-1) method
                    while(iterator3.hasNext())
                    {
                        Set<String> s = iterator3.next();
                        for(String string : s)
                        {
                            entryList.add(string);
                        }
                    }
                    preCkList = getSubsets(entryList);
                }
                else{
                    // generate candidate itemsets use F(k-1)*F(1)
                    while(iterator3.hasNext())
                    {
                        Set<String> s = iterator3.next();
                        for(String string1 : F1){
                            entryList.add(string1);
                            for(String string : s)
                            {
                                entryList.add(string);
                            }
                        }
                        String element = "";
                        for(int i=0; i<entryList.size();i++){
                            element = entryList.get(i)+element;
                        }
                        entryList.clear();
                        Set<String> elements = new HashSet<String>();
                        elements.add(element);
                        preCkList.add(elements);
                    }
                }
                System.out.println("2. The number of candidate itemsets before pruning: "+ preCkList.size());
                //pruning candidates not included in previous level
                for(int i = 0; i < preCkList.size(); i ++)
                {
                    if(preCkList.get(i).size() == k+1)
                    {
                        ckList.add(preCkList.get(i));
                    }
                }

                for(int i = 0; i < ckList.size(); i ++)
                {
                    //reducing candidate
                    if(!frequentItemSetList.get(k-1).containsAll(ckList.get(i)))
                    {
                        ckList.remove(i);
                        continue;
                    }
                }
                System.out.println("3. The number of candidate itemsets after pruning: "+ ckList.size());
                System.out.println("4. The candidate itemsets after pruning is:");

                for(int i = 0; i<ckList.size(); i++) {
                    System.out.println(ckList.get(i));
                }
                //candidates contained in transactions
                for(int i = 0; i < ckList.size(); i ++)
                {
                    int count = 0;
                    for(int j = 0; j < transactionList.size(); j ++)
                    {
                        if(transactionList.get(j).containsAll(ckList.get(i)))
                            count ++;
                    }
                    if((double)((double)count/transactionList.size()) > minSupport)
                    {
                        addTofrequentItemSetList.add(ckList.get(i));
                    }
                }
                k ++;
                frequentItemSetList.add(addTofrequentItemSetList);
                System.out.println("5. The frequent itemsets is:");
                for(int i = 0; i<frequentItemSetList.size()-1; i++) {
                    System.out.println(frequentItemSetList.get(i));
                }
            }

            //6.2 & 6.3
            frequentItemSetList.remove(frequentItemSetList.size()-1);
            Set<Set<String>> tempSet = frequentItemSetList.get(0);
            Iterator<Set<String>> tempIter = tempSet.iterator();
            Set<String> tempTempSet = tempIter.next();
            Iterator<String> tempTempIter = tempTempSet.iterator();
            Map<String, Integer> refMap = new HashMap<String, Integer>();
            while(tempTempIter.hasNext())
            {
                String enterStringInSet = tempTempIter.next();
                Set<String> newEntry = new HashSet<String>();
                newEntry.add(enterStringInSet);
                Integer newEntryCount = -1;
                Iterator<Entry<String, Integer>> innerIter = itemSupportMap.entrySet().iterator();
                while(innerIter.hasNext())
                {
                    Entry<String, Integer> entry = innerIter.next();
                    String key = (String)entry.getKey();
                    Integer value = (Integer)entry.getValue();
                    if(key.equals(enterStringInSet))
                    {
                        newEntryCount = value;
                        refMap.put(key, value);
                    }
                }
                frequentItemSupportMap.put(newEntry, newEntryCount);
            }

            //counting frequentItemSupportMap with k > 1
            for(int i = 1; i < frequentItemSetList.size(); i ++)
            {
                Set<Set<String>> setSetString = frequentItemSetList.get(i);
                Iterator<Set<String>> innerIter = setSetString.iterator();
                while(innerIter.hasNext())
                {
                    Set<String> keySet = innerIter.next();
                    Integer supportValue = 0;
                    supportValue = getSupportCount(keySet);
                    frequentItemSupportMap.put(keySet, supportValue);
                }
            }
            //generating confidences:
            //get all rule possibilities in tempRuleList
            List<Rule> tempRuleList = new ArrayList<Rule>();
            Iterator<Entry<Set<String>, Integer>> iter = frequentItemSupportMap.entrySet().iterator();
            while(iter.hasNext())
            {
                Entry<Set<String>, Integer> entry = iter.next();
                Set<String> key = (Set<String>)entry.getKey();
                Integer value = (Integer)entry.getValue();
                Iterator<Entry<Set<String>, Integer>> inneriter = frequentItemSupportMap.entrySet().iterator();
                while(inneriter.hasNext())
                {
                    Entry<Set<String>, Integer> innerEntry = inneriter.next();
                    Set<String> innerKey = (Set<String>)innerEntry.getKey();
                    Integer innerValue = (Integer)innerEntry.getValue();

                    if(union(key, innerKey).size() != 0 && innerKey.size() == 1)
                    {
                        Rule rule = new Rule(transactionList);
                        rule.setLhs(key);
                        rule.setRhs(innerKey);
                        rule.setLhsCount(value);
                        rule.setRhsCount(innerValue);
                        tempRuleList.add(rule);
                    }
                }
            }

            //Pruning rules
            for(int i = 0; i < tempRuleList.size(); i ++)
            {
                Rule currentRule = tempRuleList.get(i);
                if(currentRule.getLhs().containsAll(currentRule.getRhs()))
                {
                    tempRuleList.remove(currentRule);
                    continue;
                }
                if(currentRule.computeAndGetConfidence() < minConfidence)
                {
                    tempRuleList.remove(currentRule);
                    continue;
                }
                ruleList.add(currentRule);
            }
            sortedSupportMap.putAll(frequentItemSupportMap);

            Comparator<Rule> comparator = new Comparator<Rule>() {
                public int compare(Rule o1, Rule o2) {
                    if(o1.confidence <= o2.confidence)
                        return 1;
                    return -1;
                }
            };
            Collections.sort(ruleList, comparator);
            System.out.println("\n\nRules List :");
            for(int i = 0; i < ruleList.size(); i ++)
            {
                System.out.println(ruleList.get(i));
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private static int getSupportCount(Set<String> set)
    {
        int count = 0;
        for(int i = 0; i < transactionList.size(); i ++)
        {
            if(transactionList.get(i).containsAll(set))
            {
                count ++;
            }
        }
        return count;
    }
}

class Rule
{
    Set<String> lhs;
    Set<String> rhs;
    int lhsCount;
    int rhsCount;
    double confidence;
    int support;
    List<List<String>> transactionList = new ArrayList<List<String>>();

    Rule(List<List<String>> tl)
    {
        transactionList = tl;
        lhs = new HashSet<String>();
        rhs = new HashSet<String>();
    }

    private int getSupportCount(Set<String> set)
    {
        int count = 0;
        for(int i = 0; i < transactionList.size(); i ++)
        {
            if(transactionList.get(i).containsAll(set))
            {
                count ++;
            }
        }
        return count;
    }

    public void setLhs(Set<String> lhs) {
        this.lhs = lhs;
    }

    public Set<String> getLhs() {
        return lhs;
    }

    @Override
    public String toString() {
        return Arrays.toString(lhs.toArray()) + " ==> " + Arrays.toString(rhs.toArray());
    }

    public int getLhsCount() {
        return lhsCount;
    }

    public Set<String> getRhs() {
        return rhs;
    }

    public int getRhsCount() {
        return rhsCount;
    }

    public void setLhsCount(int lhsSupport) {
        this.lhsCount = lhsSupport;
    }

    public void setRhs(Set<String> rhs) {
        this.rhs = rhs;
    }

    public void setRhsCount(int rhsSupport) {
        this.rhsCount = rhsSupport;
    }

    public double computeAndGetConfidence()
    {
        confidence = getSupportCount(union(lhs, rhs));
        support = (int)confidence;
        confidence /= getSupportCount(lhs);
        return confidence;
    }

    public <T>Set<T> union(Set<T> setA, Set<T> setB) {
        Set<T> tmp = new HashSet<T>(setA);
        tmp.addAll(setB);
        return tmp;
    }
}

class ValueComparator implements Comparator<Set<String>> {

    Map<Set<String>, Integer> base;
    public ValueComparator(Map<Set<String>, Integer> base) {
        this.base = base;
    }

    public int compare(Set<String> a, Set<String> b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        }
    }
}