package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class DataParser {

	/**
	 * List of all the sequences in the data file
	 */
	List<Sequence> m_ListofSequences;
	
	/**
	 * itm and mis key value pair
	 */
	Map<Integer,Double> m_ItmMISMap;
	
	/**
	 * Returns the parsed sequence
	 * @return
	 */
	public void parseDataFile(String pathname){
		m_ListofSequences = new ArrayList<DataParser.Sequence>();
		
		File datafile = new File(pathname);
		try {
			FileReader r = new FileReader(datafile);
			BufferedReader reader = new BufferedReader(r); 
			
			String data = null;
			while( (data = reader.readLine()) != null){
				
				//remove trailing spaces
				data = data.trim();
				
				//<{18, 23, 37, 44}{18, 46}{17, 42, 44, 49}>
				//String regex = "(\\{(\\d+,\\s*)*\\d+\\})+"; This is for entire sequence
				
				String regex = "\\}"; //splitter regex
				Pattern pat =  Pattern.compile(regex);
				data = data.replaceAll("[<>]", "");
				String[] arr = pat.split(data);
				
				//add itm set to sequence
				Sequence seq = new Sequence();
				
				//create itemset and add to sequence
				for(int i = 0; i < arr.length; i++){
					arr[i] = arr[i].replaceAll("\\{", "").trim(); //remove braces => ItemSet
					
					//create a itemset
					ItemSet itmSet = new ItemSet();
					String[] itmArr = arr[i].split(",");
					
					for(int j = 0; j < itmArr.length; j++){
						Item itm = new Item();
						int value = Integer.parseInt(itmArr[j].trim());
						itm.setM_item(value);
						itmSet.addItem(itm);
					}
					
					//add itemset to sequence
					seq.addItmSet(itmSet);
				}
				
				//add sequence to the list
				m_ListofSequences.add(seq);
			}
			
			System.out.println("Total number of sequences: " + m_ListofSequences.size());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void parseMIS(String parafile) {
		
		try {
			FileReader r = new FileReader(parafile);
			BufferedReader reader = new BufferedReader(r); 
			m_ItmMISMap = new HashMap<Integer, Double>();
			String data = null;
			while( (data = reader.readLine()) != null){
				
				//no need to store support difference constraint
				if(data.contains("SDC"))
					continue;
				
				data = data.trim();
				String[] para = data.split("\\s*=\\s*");
				int itm = Integer.parseInt(para[0].replaceAll("[(MIS)()]", "").trim());
				double mis = Double.parseDouble(para[1].trim());
				
				//insert the Item, MIS value pairs
				m_ItmMISMap.put(itm, mis);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Get map of MIS and item
	 * @return
	 */
	public Map<Integer, Double> getM_ItmMISMap() {
		return m_ItmMISMap;
	}
	
	/**
	 * Get Data sequences
	 * @return
	 */
	public List<Sequence> getM_ListofSequences() {
		return m_ListofSequences;
	}

	/**
	 * Sequence is list of ItemSet, called elements of a Sequence. s = <a1, a2,... ar> where a1, a2,..ar are elements(Itemsets)
	 * @author kandur
	 *
	 */
	public class Sequence{
		
		List<ItemSet> m_ItmSet = null;
		int m_Seqid = -1;
		int m_Count = -1;
		
		public Sequence() {
			m_ItmSet = new ArrayList<DataParser.ItemSet>();
		}
		
		public List<ItemSet> getM_ItmSet() {
			return m_ItmSet;
		}
		
		public void setM_ItmSet(List<ItemSet> itmSetList) {
			this.m_ItmSet = itmSetList;
		}
		
		public int getM_seqid() {
			return m_Seqid;
		}
		
		public void setM_seqid(int seqid) {
			this.m_Seqid = seqid;
		}
		
		public void addItmSet(ItemSet itmSet){
			this.m_ItmSet.add(itmSet);
		}
		
		public boolean equals(Sequence s2){
			List<ItemSet> listofISs1 = this.m_ItmSet;
			List<ItemSet> listofISs2 = s2.m_ItmSet;
			
			//get all the items in sequence 1
			List<Item> allItemsIns1 = new ArrayList<DataParser.Item>();
			for(int i = 0; i < listofISs1.size(); i++){
				Set<Item> s1Items = listofISs1.get(i).getM_ItemSet();
				Iterator<Item> itr = s1Items.iterator();
				while(itr.hasNext())
					allItemsIns1.add(itr.next());
			}
			
			//get all the items in sequence 2
			List<Item> allItemsIns2 = new ArrayList<DataParser.Item>();
			for(int j = 0; j < listofISs2.size(); j++){
				Set<Item> s2Items = listofISs2.get(j).getM_ItemSet();
				Iterator<Item> itr = s2Items.iterator();
				while(itr.hasNext())
					allItemsIns2.add(itr.next());
			}
			
			//now compare all the items of sequence 1 and 2
			if(allItemsIns1.size() != allItemsIns2.size())
				return false;
			
			return allItemsIns1.equals(listofISs2);
		}
	}
	
	/**
	 * ItemSet is set of Items
	 * @author kandur
	 *
	 */
	public class ItemSet{
		
		Set<Item> m_ItemSet = null;
		
		public ItemSet() {
			m_ItemSet = new LinkedHashSet<DataParser.Item>();
		}
		
		public Set<Item> getM_ItemSet() {
			return m_ItemSet;
		}
		
		public void setM_ItemSet(Set<Item> itemSet) {
			this.m_ItemSet = itemSet;
		}
		
		public void addItem(Item itm){
			this.m_ItemSet.add(itm);
		}
		
		/**
		 * Equality checking
		 * @param is2
		 * @return
		 */
		boolean equals(ItemSet is2){
			
			//if size differs
			if(this.m_ItemSet.size() != is2.m_ItemSet.size())
				return false;
			
			//At this point size of both ItemSet are equal
			Iterator<Item> itr1 = this.m_ItemSet.iterator();
			Iterator<Item> itr2 = is2.m_ItemSet.iterator();
			//check for equality
			while(itr1.hasNext()){
				if(!itr1.next().equals(itr2.next()))
					return false;
				else 
					continue;
			}
			
			//all good
			return true;
		}
	}
	
	/**
	 * Individual value/entity of an ItemSet
	 * @author kandur
	 *
	 */
	public class Item{
	
		int m_item = -1;
		
		public Item() {
			
		}
		
		 public Item(int value) {
			this.m_item = value;
		}
		 
		public int getItem() {
			return m_item;
		}
		
		public void setM_item(int value) {
			this.m_item = value;
		}
		
		/**
		 * Equality checking
		 * @param i2
		 * @return
		 */
		boolean equals(Item i2){
			if(this.m_item == i2.m_item)
				return true;
			
			return false;
		}
	}
	
	public static void main(String[] args){
		DataParser parser = new DataParser();
		//parser.parseDataFile("./data/data.txt");
		parser.parseMIS("./data/para.txt");
	}
}


