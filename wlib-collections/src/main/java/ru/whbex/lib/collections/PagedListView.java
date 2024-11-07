package ru.whbex.lib.collections;

import java.util.Collections;
import java.util.List;
// Source: [Clans] ru.whbex.develop.clans.common.misc

/**
 * Paginated list viewer
 * @param <E> list element type
 */
// TODO: Improve docs
public class PagedListView<E> {
    private final static short PAGE_SIZE = 10;
    private final short pageSize;
    private final List<E> list;
    public PagedListView(List<E> list){
        this(list, PAGE_SIZE);
    }
    public PagedListView(List<E> list, short pageSize){
        this.pageSize = pageSize;
        this.list = list;

    }

    /**
     * Get size of list
     * @return backed list size
     */
    public int size(){
        return list.size();
    }

    /**
     * Get current list page amount
     * @return
     */
    public int pageAmount(){
        if(list.isEmpty())
            return 0;
        return list.size() % PAGE_SIZE != 0 ? list.size() / PAGE_SIZE + 1 : list.size() / PAGE_SIZE;
    }

    /**
     * Get specified page
     * @param pageIndex page index
     * @return sublist with elements included in specified page
     * @throws ArrayIndexOutOfBoundsException if page index is less than 1 or greater than page amount
     */
    public List<E> page(int pageIndex) throws ArrayIndexOutOfBoundsException {
        if(list.isEmpty())
            return Collections.emptyList();
        int pageAmount = pageAmount();
        if(pageIndex < 1 || pageIndex > pageAmount)
            throw new ArrayIndexOutOfBoundsException("Page index " + pageIndex + "out of bounds");
        // start index
       // int si = (pageIndex - 1 == 0 ? 1 : (pageIndex - 1) * PAGE_SIZE) - 1; wtf lol
        int si = (pageIndex - 1) * PAGE_SIZE;
        // end index
       // int ei = (si + PAGE_SIZE) - pageIndex == pageAmount ? (PAGE_SIZE * pageAmount() - size()) : 0;
        int ei = si + PAGE_SIZE;
        // check for last page fullness
        if(pageIndex == pageAmount && size() != pageAmount * PAGE_SIZE)
            ei = ei - (pageAmount * PAGE_SIZE - size());
        return list.subList(si, ei);
    }
}
