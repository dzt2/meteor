package lab.meteor.core;

import java.net.UnknownHostException;
import java.util.List;

import lab.meteor.core.MDBAdapter.IDList;
import lab.meteor.dba.MongoDBAdapter;

import com.mongodb.DB;
import com.mongodb.Mongo;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Mongo m = new Mongo("localhost", 27017);
			MDatabase db=MDatabase.getDB();
			DB mdb =m.getDB("meteor");
			MDBAdapter adapter=new MongoDBAdapter(mdb);
			db.setDBAdapter(adapter);
			db.initialize();
			
			//IDList list=adapter.listAllClassIDs();
			//IDList list=adapter.listAllAttributeIDs();
			//IDList list=adapter.listAllReferenceIDs();
			//IDList list=adapter.listAllEnumIDs();
			//IDList list=adapter.listAllSymbolIDs();
			IDList list=adapter.listAllClassIDs();
			
			System.out.println("There are "+list.size()+" classes.");
			for(int i=0;i<list.size();i++){
				MClass cls=db.getClass(list.get(i));
				List<Long> olist=db.getObjects(cls.getID());
				//System.out.println(cls.getName()+": "+olist.size());
				for(int j=0;j<olist.size();j++){
					MObject obj=db.getObject(olist.get(j));
					MClass ocls=obj.getClazz();
					
					System.out.print("["+ocls.getName()+"]: ");
					String[] attrs=ocls.getAllAttributeNames();
					for(int k=0;k<attrs.length;k++)
						System.out.print(attrs[k]+":"+obj.get(attrs[k])+" ");
					System.out.println();
					
					attrs=ocls.getReferenceNames();
					for(int k=0;k<attrs.length;k++)
						System.out.print(attrs[k]+":"+obj.get(attrs[k])+" ");
					System.out.println();
				}
				
				
				/*MEnum e=db.getEnum(list.get(i));
				printEnum(e);*/
				/*MReference ref=db.getReference(list.get(i));
				printReference(ref);*/
				/*MAttribute attr=db.getAttribute(list.get(i));
				printAttribute(attr);*/
				/*MClass cls=db.getClass(list.get(i));
				printClass(cls);*/
				
				/*ClassDBInfo info=new ClassDBInfo();
				info.id=list.get(i);
				adapter.loadClass(info);
				//adapter.loadClass(info);
				//System.out.println(info.id+": "+info.name);
				MClass cls=new MClass(info.name);
				cls.load();*/
				//MElementType type=adapter.getElementType(list.get(i));
				//System.out.println(type.name());
				//MClass cls=new MClass(list.get(i));
				//cls.load();
				//cls.forceLoad();
				//System.out.println("Name: "+cls.getName());
				//System.out.println(type.name());
				
				/*ClassDBInfo info=new ClassDBInfo();
				info.id=list.get(i);
				adapter.loadClass(info);
				System.out.println("["+info.id+"]: "+info.name);*/
				
				//System.out.println("["+i+"]: "+list.get(i));
				//MElementType c=adapter.get
				//System.out.println("["+i+"]: "+c.get);
			}
			//MPackage pck=new MPackage("dzt");
			//car.setPackage(pck);
			//pck.save();
			//car.save();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
	}
	
	public static void printSymbol(MSymbol symbol){
		if(symbol==null)return;
		System.out.println(symbol.getName()+"["+symbol.getEnum()+"]");
	}
	
	public static void printEnum(MEnum e){
		if(e==null)return;
		System.out.println("------------- "+e.getPackage()+":"+e.getName()+" --------------");
		System.out.print("Attributes: ");
		String[] attrs=e.getSymbolNames();
		if(attrs!=null)
			for(int i=0;i<attrs.length;i++){
				MSymbol symbol=e.getSymbol(attrs[i]);
				System.out.print(symbol.getName()+" ");
			}
		System.out.println();
		
		System.out.println("Native Data Type: "+e.getNativeDataType());
		System.out.println("Type Identify: "+e.getTypeIdentifier());
	}
	
	public static void printReference(MReference ref){
		if(ref==null)return;
		System.out.println(ref.getName()+"["+ref.getType()+"]: "+ref.getOwner());
	}
	
	public static void printAttribute(MAttribute attr){
		if(attr==null)return;
		System.out.println(attr.getName()+"["+attr.getType()+"]: "+attr.getOwner());
	}
	
	public static void printClass(MClass cls){
		if(cls==null)return;
		System.out.println("--------------- "+cls.getName()+" ---------------");
		System.out.print("Attributes: ");
		String[] attrs=cls.getAllAttributeNames();
		if(attrs!=null){
			for(int i=0;i<attrs.length;i++){
				MAttribute attr=cls.getAttribute(attrs[i]);
				System.out.print(attr.getName()+"("+attr.getType()+") \t");
			}
		}
		System.out.println();
		
		String[] refs=cls.getAllReferenceNames();
		System.out.print("Reference: ");
		if(refs!=null){
			for(int i=0;i<refs.length;i++){
				MReference ref=cls.getReference(refs[i]);
				System.out.print(ref.getName()+"("+ref.getType()+") \t");
			}
		}
		System.out.println();
	}

}
