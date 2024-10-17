package Practice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

class TreeNode {
    String name;
    int lockBy;
    boolean isLoc;
    List<TreeNode> childs;
    TreeNode parent;
    Set<TreeNode> locDesc;

    TreeNode(String nm, TreeNode par) {
        name = nm;
        lockBy = -1;
        parent = par;
        isLoc = false;
        childs = new ArrayList<>();
        locDesc = new HashSet<>();
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
        int k = 1, i, n = a.size();
        q.add(root);
        while (!q.isEmpty()) {
            TreeNode r = q.poll();
            nttm.put(r.name, r);
            List<String> b = new ArrayList<>();
            for (i = k; i < Math.min(n, k + m); i++) {
                b.add(a.get(i));
            }
            r.addChild(b);
            for (TreeNode child : r.childs) {
                q.add(child);
            }
            k = i;
        }
    }

    void updateParents(TreeNode r, TreeNode curr) {
        while (r != null) {
            r.locDesc.add(curr);
            r = r.parent;
        }
    }

    boolean lock(String name, int id) {
        TreeNode r = nttm.get(name);
        if (r == null || r.isLoc || !r.locDesc.isEmpty()) {
            return false;
        }
        TreeNode par = r.parent;
        while (par != null) {
            if (par.isLoc) {
                return false;
            }
            par = par.parent;
        }
        updateParents(r.parent, r);
        r.isLoc = true;
        r.lockBy = id;
        return true;
    }
    boolean unlock(String name, int id) {
        TreeNode r = nttm.get(name);
        if (r == null || !r.isLoc || r.lockBy != id) {
            return false;
        }
        TreeNode par = r.parent;
        while (par != null) {
            par.locDesc.remove(r);
            par = par.parent;
        }
        r.isLoc = false;
        r.lockBy = -1;
        return true;
    }

    boolean upgrade(String name, int id) {
        TreeNode r = nttm.get(name);
        if (r == null || r.isLoc || r.locDesc.isEmpty()) {
            return false;
        }
        for (TreeNode ld : r.locDesc) {
            if (ld.lockBy != id) {
                return false;
            } 
        }
        Set<TreeNode> stt = new HashSet<>(r.locDesc);
        for (TreeNode ld : stt) {
            unlock(ld.name, id);
        }
        lock(name, id);
        return true;
    }
}

public class Approach1 {
    public static void main(String args[]){
        Scanner sc= new Scanner(System.in);
        int n, m, q, ot, id;
        String name;
        n= sc.nextInt();
        m= sc.nextInt();
        q= sc.nextInt();
        List<String> a= new ArrayList<>();
        for(int i=0;i<n;i++)
        {
            a.add(sc.next());
        }
        MTree tree= new MTree(a.get(0));
        tree.makeMTree(a, m);
        while(q-->0){
            ot= sc.nextInt();
            name = sc.next();
            id= sc.nextInt();
            if(ot==1){
                System.out.println(tree.lock(name,id)?"true" : "false");
            }
            else if(ot==2){
                System.out.println(tree.unlock(name,id)? "true": "false");   
            }   
            else if(ot==3)
            {   
                System.out.println(tree.upgrade(name,id)?"true": "false");
            }
        }
        sc.close();
    }
}
