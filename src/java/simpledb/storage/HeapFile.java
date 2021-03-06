package simpledb.storage;

import simpledb.common.Database;
import simpledb.common.DbException;
import simpledb.common.Debug;
import simpledb.common.Permissions;
import simpledb.transaction.TransactionAbortedException;
import simpledb.transaction.TransactionId;

import java.io.*;
import java.util.*;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {

    private final File f;
    private final TupleDesc td;

    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */
    public HeapFile(File f, TupleDesc td) {
        // some code goes here
        this.f = f;
        this.td = td;
    }

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here
        return f;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere to ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // some code goes here
        return f.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
        return td;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        // some code goes here
        int tableid = pid.getTableId();
        int pgNo = pid.getPageNumber();
        final int pageSize = BufferPool.getPageSize();
        byte[] rawPageData = HeapPage.createEmptyPageData();

        // random access read from disk
        try {
            FileInputStream in = new FileInputStream(f);
            in.skip(pgNo * pageSize);
            in.read(rawPageData);
            return new HeapPage(new HeapPageId(tableid, pgNo), rawPageData);
        } catch (IOException e) {
            throw new IllegalArgumentException("HeapFile: readPage: file not found");
        }
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for lab1
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // some code goes here
        int fileSizeInByte = (int) f.length();
        return fileSizeInByte / BufferPool.getPageSize();
    }

    // see DbFile.java for javadocs
    public List<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        // some code goes here
        return new HeapFileIterator(tid);
    }

    private class HeapFileIterator implements DbFileIterator {
        private Integer pgCursor;
        private Iterator<Tuple> tupleItr;
        private final TransactionId tid;
        private final int tableid;
        private final int numPages;

        public HeapFileIterator(TransactionId tid) {
            this.pgCursor = null;
            this.tupleItr = null;
            this.tid = tid;
            this.tableid = getId();
            this.numPages = numPages();
        }

        private Iterator<Tuple> getTupleItr(int pgNo) throws TransactionAbortedException, DbException{
            PageId pid = new HeapPageId(tableid, pgNo);
            return ((HeapPage) Database.getBufferPool().getPage(tid, pid, Permissions.READ_ONLY)).iterator();
        }

        @Override
        public void open() throws DbException, TransactionAbortedException {
            pgCursor = 0;
            tupleItr = getTupleItr(pgCursor);
        }

        @Override
        public boolean hasNext() throws DbException, TransactionAbortedException {
            if (pgCursor != null) {
                while (pgCursor < numPages - 1) {
                    if (tupleItr.hasNext()) {
                        return true;
                    } else {
                        pgCursor += 1;
                        tupleItr = getTupleItr(pgCursor);
                    }
                }
                return tupleItr.hasNext();
            } else {
                return false;
            }
        }

        @Override
        public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
            if (hasNext()) {
                return tupleItr.next();
            }
            throw new NoSuchElementException("HeapFileIterator: error: no more elements");
        }

        @Override
        public void rewind() throws DbException, TransactionAbortedException {
            close();
            open();
        }

        @Override
        public void close() {
            pgCursor = null;
            tupleItr = null;
        }
    }

}

