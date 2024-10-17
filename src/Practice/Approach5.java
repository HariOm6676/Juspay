package Practice;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

class TreeNode {
    String name;
    int lockBy;
    boolean isLoc;
    List<TreeNode> childs;
    TreeNode parent;
    Set<TreeNode> locDesc;
    AtomicInteger version; // For optimistic locking

    TreeNode(String nm, TreeNode par) {
        name = nm;
        lockBy = -1;
        parent = par;
        isLoc = false;
        childs = new ArrayList<>();
        locDesc = new HashSet<>();
        version = new AtomicInteger(0); // Initialize version to 0
    }

    void addChild(List<String> a) {
        for (String nm : a) {
            childs.add(new TreeNode(nm, this));
        }
    }
}

class MTree {
    TreeNode root;
    Map<String, TreeNode> nttm;

    MTree(String name) {
        root = new TreeNode(name, null);
        nttm = new HashMap<>();
    }

    void makeMTree(List<String> a, int m) {
        Queue<TreeNode> q = new LinkedList<>();
        q.add(root);
        int k = 1, i, n = a.size();
        while (!q.isEmpty()) {
            TreeNode r = q.poll();
            nttm.put(r.name, r);
            List<String> b = new ArrayList<>();
            for (i = k; i < Math.min(n, m + n); i++) {
                b.add(a.get(i));
            }
            r.addChild(b);
            for (TreeNode child : r.childs) {
                q.add(child);
            }
            k = i;
        }
    }

    boolean lock(String name, int id){
        TreeNode r = nttm.get(name);
        if(r==null){
            return false;
        }
        int currentVersion= r.version.get();
        if(r.isLoc || !r.locDesc.isEmpty()){
            return false;
        }
        TreeNode par = r.parent;
        while(par!=null){
            if(par.isLoc){
                return false;
            }
            par= par.parent;
        }
        if(r.version.compareAndSet(currentVersion, currentVersion+1)){
            r.isLoc = true;
            r.lockBy = id;
            updateParents(r.parent, r);
            return true;
        }
        else{
            return false;
        }
    }

    boolean unlock(String name, int id){
        TreeNode r= nttm.get(name);
        if(r==null)
        return false;
        int currentVersion = r.version.get();
        if(!r.isLoc || r.lockBy != id)
        return false;
        if(r.version.compareAndSet(currentVersion, currentVersion+1)){
            r.isLoc = false;
            r.lockBy= -1;
            TreeNode par = r.parent;
            while(par!=null)
            {
                par.locDesc.remove(r);
                par= par.parent;
            }
            return true;
        }
        else{
            return false;
        }
    }

    boolean upgrade(String name, int id){
        TreeNode r= nttm.get(name);
        if(r==null || r.isLoc || r.locDesc.isEmpty())
        return false;
        int currentVersion = r.version.get();
        for(TreeNode ld: r.locDesc){
            if(ld.lockBy!= id)
            {
                return false;
            }
        }
        if(r.version.compareAndSet(currentVersion, currentVersion+1)){
            Set<TreeNode> stt = new HashSet<>(r.locDesc);
            for( TreeNode ld: stt){
                unlock(ld.name, id);
            }
            return lock(name, id);
        }
        else{
            return false;
        }
    }

}
public class Approach5 {
    
}
