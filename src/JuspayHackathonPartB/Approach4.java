package JuspayHackathonPartB;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// Represents a tree node with locking capabilities
class TreeNode {

    String name;                // Node name
    int lockBy;                 // ID of the user who locked this node
    boolean isLoc;              // Whether the node is locked
    List<TreeNode> childs;      // List of child nodes
    TreeNode parent;            // Parent node
    Set<TreeNode> locDesc;      // Set of locked descendants
    ReadWriteLock lock;         // Read-write lock for the node

    TreeNode(String nm, TreeNode par) {
        name = nm;
        lockBy = -1;            // Initially, no one has locked the node
        parent = par;
        isLoc = false;
        childs = new ArrayList<>();
        locDesc = new HashSet<>();
        lock = new ReentrantReadWriteLock();  // Initialize the read-write lock
    }

    // Add children to this node
    void addChild(List<String> a) {
        for (String nm : a) {
            childs.add(new TreeNode(nm, this));
        }
    }
}

// Represents the M-ary tree structure with locking mechanisms
class MTree {
    TreeNode root;              // Root node of the tree
    Map<String, TreeNode> nttm; // Map from node name to TreeNode object

    MTree(String name) {
        root = new TreeNode(name, null);
        nttm = new HashMap<>();
    }

    // Create an M-ary tree from a list of names
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

    // Update the locDesc set of all parent nodes
    void updateParents(TreeNode r, TreeNode curr) {
        while (r != null) {
            r.lock.writeLock().lock();  // Acquire write lock for the parent node
            try {
                r.locDesc.add(curr);    // Safely update the locDesc set
            } finally {
                r.lock.writeLock().unlock();  // Release the write lock
            }
            r = r.parent;
        }
    }
    

    // Lock a node and its ancestors if possible
    boolean lock(String name, int id) {
        TreeNode r = nttm.get(name);
        if (r == null) {
            return false;
        }
        r.lock.writeLock().lock();  // Acquire write lock for the node
        try {
            if (r.isLoc || !r.locDesc.isEmpty()) 
                return false;
            TreeNode par = r.parent;
            while (par != null) {
                par.lock.readLock().lock();  // Acquire read lock for ancestor nodes
                try {
                    if (par.isLoc) return false;
                } finally {
                    par.lock.readLock().unlock();
                }
                par = par.parent;
            }
            updateParents(r.parent, r);  // Update locked descendants
            r.isLoc = true;
            r.lockBy = id;  // Mark the node as locked by the given ID
            return true;
        } finally {
            r.lock.writeLock().unlock();  // Release write lock
        }
    }

    // Unlock a node if it is locked by the given ID
    boolean unlock(String name, int id) {
        TreeNode r = nttm.get(name);
        if (r == null)
            return false;
        r.lock.writeLock().lock();  // Acquire write lock for the node
        try {
            if (!r.isLoc || r.lockBy != id) {
                return false;
            }
            TreeNode par = r.parent;
            while (par != null) {
                par.lock.readLock().lock();  // Acquire read lock for ancestor nodes
                try {
                    par.locDesc.remove(r);  // Remove node from locked descendants
                } finally {
                    par.lock.readLock().unlock();
                }
                par = par.parent;
            }
            r.isLoc = false;
            r.lockBy = -1;  // Mark the node as unlocked
            return true;
        } finally {
            r.lock.writeLock().unlock();  // Release write lock
        }
    }

    // Upgrade the lock of a node if all its descendants are locked by the same ID
    boolean upgrade(String name, int id) {
        TreeNode r = nttm.get(name);
        if (r == null || r.isLoc || r.locDesc.isEmpty()) {
            return false;
        }
        r.lock.writeLock().lock();  // Acquire write lock for the node
        try {
            for (TreeNode ld : r.locDesc) {
                ld.lock.readLock().lock();  // Acquire read lock for descendants
                try {
                    if (ld.lockBy != id) {
                        return false;
                    }
                } finally {
                    ld.lock.readLock().unlock();  // Release read lock
                }
            }

            // Unlock descendants and then lock the node
            Set<TreeNode> stt = new HashSet<>(r.locDesc);
            for (TreeNode ld : stt) {
                unlock(ld.name, id);
            }
            return lock(name, id);  // Lock the node after unlocking descendants
        } finally {
            r.lock.writeLock().unlock();  // Release write lock
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
