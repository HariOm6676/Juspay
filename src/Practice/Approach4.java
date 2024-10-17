package Practice;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class TreeNode {
    String name;
    int lockBy;
    boolean isLoc;
    List<TreeNode> childs;
    TreeNode parent;
    Set<TreeNode> locDesc;
    ReadWriteLock lock;

    TreeNode(String nm, TreeNode par) {
        name = nm;
        lockBy = -1;
        parent = par;
        isLoc = false;
        childs = new ArrayList<>();
        locDesc = new HashSet<>();
        lock = new ReentrantReadWriteLock();
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

    void updateParents(TreeNode r, TreeNode curr) {
        while (r != null) {
            r.lock.writeLock().lock();
            try {
                r.locDesc.add(curr);
            } finally {
                r.lock.writeLock().unlock();
            }
            r = r.parent;
        }
    }

    boolean lock(String name, int id) {
        TreeNode r = nttm.get(name);
        if (r == null) {
            return false;
        }
        r.lock.writeLock().lock();
        try {
            if (r.isLoc || !r.locDesc.isEmpty())
                return false;
            TreeNode par = r.parent;
            while (par != null) {
                par.lock.readLock().lock();
                try {
                    if (par.isLoc)
                        return false;
                } finally {
                    par.lock.readLock().unlock();
                }
                par = par.parent;
            }
            updateParents(r.parent, r);
            r.isLoc = true;
            r.lockBy = id;
            return true;
        } finally {
            r.lock.writeLock().unlock();
        }
    }
    boolean unlock(String name, int id){
        TreeNode r = nttm.get(name);
        if(r==null)
        return false;
        r.lock.writeLock().lock();
        try{
            if(!r.isLoc || r.lockBy!=id)
            return false;    
            TreeNode par = r.parent;
            while(par != null)
            {
                par.lock.writeLock();
                try{
                    par.locDesc.remove(r);
                }finally{
                    par.lock.writeLock().unlock();
                }
                par = par.parent;
            }
            r.isLoc = false;
            r.lockBy = 0;
            return true;
        }
        finally{
            r.lock.writeLock().unlock();
        }
    }

    boolean upgrade(String name, int id)
    {
        TreeNode r= nttm.get(name);
        if(r==null || r.isLoc || r.locDesc.isEmpty()){
            return false;
        }
        r.lock.writeLock().lock();
        try{
            for(TreeNode ld : r.locDesc){
                ld.lock.readLock().lock();
                try {
                    if(ld.lockBy != id)
                    return false;
                } finally {
                    ld.lock.readLock().unlock();
                }
            }
            Set<TreeNode> stt= new HashSet<>(r.locDesc);
            for (TreeNode ld: stt){
                unlock(ld.name, id);
            }
            return lock(name,id);
        }finally{
            r.lock.writeLock().unlock();
        }
    }
}
public class Approach4 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();  // Number of nodes
        int m = sc.nextInt();  // Maximum number of children for each node
        int q = sc.nextInt();  // Number of operations
        List<String> a = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            a.add(sc.next());
        }
        MTree tree = new MTree(a.get(0));  // Create tree with root
        tree.makeMTree(a, m);  // Build the tree

        // Create a thread pool with 4 threads
        ExecutorService executor = Executors.newFixedThreadPool(4);

        Runnable task = () -> {
            Random rand = new Random();
            for (int i = 0; i < q; i++) {
                int ot = rand.nextInt(3) + 1;  // Random operation: 1 for lock, 2 for unlock, 3 for upgrade
                String name = a.get(rand.nextInt(n));  // Random node name
                int id = rand.nextInt(3);  // Random ID
                boolean result = false;
                switch (ot) {
                    case 1:
                        result = tree.lock(name, id);  // Lock operation
                        break;
                    case 2:
                        result = tree.unlock(name, id);  // Unlock operation
                        break;
                    case 3:
                        result = tree.upgrade(name, id);  // Upgrade operation
                        break;
                }
                System.out.println("Thread " + Thread.currentThread().getName() +
                    " Operation: " + ot + " Node: " + name + " ID: " + id + " Result: " + result);
                try {
                    Thread.sleep(500);  // Sleep for 500ms between operations
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        // Submit multiple tasks to the thread pool
        for (int i = 0; i < 4; i++) {
            executor.submit(task);
            try {
                Thread.sleep(500);  // Sleep for 500ms between task submissions
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Shut down the executor service
        executor.shutdown();
        sc.close();
    }
}
