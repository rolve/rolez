package ch.trick17.rolez.generic

import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleParam
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.TypeParam
import com.google.common.collect.ForwardingListIterator
import java.util.Collection
import java.util.List
import java.util.Map
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.EObject

import static extension com.google.common.collect.Iterators.advance

package abstract class ParameterizedEList<E extends EObject, C extends EObject> extends Parameterized implements EList<E> {
    
    package val List<E> list
    package val C eContainer
    
    new(List<E> list, C eContainer, Map<TypeParam, Type> typeArgs, Map<RoleParam, Role> roleArgs) {
        super(typeArgs, roleArgs)
        if(list === null || eContainer === null)
            throw new NullPointerException
        this.list = list
        this.eContainer = eContainer
    }
    
    new(ParameterizedEList<E, C> base, int from, int to) {
        super(base)
        this.list = base.list.subList(from, to)
        this.eContainer = base.eContainer
    }
    
    def E parameterize(E e)
    
    /* Delegate to original list, if possible */
    
    override size()         { list.size }
    override isEmpty()      { list.isEmpty }
    override get(int index) { list.get(index).parameterize }
    
    override toArray()          { list.toArray }
    override <T> toArray(T[] a) { list.toArray(a) }
    
    override listIterator() {
        new ForwardingListIterator<E> {
            val delegate = list.listIterator
            override protected delegate() { delegate }
            
            override next()     { delegate.next    .parameterize }
            override previous() { delegate.previous.parameterize }
            
            override add(E element) { throw new AssertionError }
            override set(E element) { throw new AssertionError }
            override remove()       { throw new AssertionError }
        }
    }
    
    /* Otherwise, use above methods */
    
    override iterator() { listIterator }
    
    override listIterator(int index) { listIterator => [advance(index)] }
        
    override indexOf(Object o) {
        for(var i = 0; i < size; i++)
            if(o == get(i))
                return i;
        -1
    }
    
    override lastIndexOf(Object o) {
        for(var i = size - 1; i >= 0; i--)
            if(o == get(i))
                return i;
        -1
    }
    
    override contains(Object o) { indexOf(o) != -1 }
    
    override containsAll(Collection<?> c) { c.forall[contains] }
    
    /* All mutating methods throw an error */
    
    override move(int newPosition, E object)              { throw new AssertionError }
    override move(int newPosition, int oldPosition)       { throw new AssertionError }
    override add(E e)                                     { throw new AssertionError }
    override add(int index, E element)                    { throw new AssertionError }
    override addAll(Collection<? extends E> c)            { throw new AssertionError }
    override addAll(int index, Collection<? extends E> c) { throw new AssertionError }
    override clear()                                      { throw new AssertionError }
    override remove(Object o)                             { throw new AssertionError }
    override remove(int index)                            { throw new AssertionError }
    override removeAll(Collection<?> c)                   { throw new AssertionError }
    override retainAll(Collection<?> c)                   { throw new AssertionError }
    override set(int index, E element)                    { throw new AssertionError }
    
    /* To be on the safe side, implement equals and hachCode */
    
    override equals(Object other) {
        if(this === other)
            true
        else if(other instanceof ParameterizedEList<?, ?>)
            super.equals(other) && list == other.list
        else
            false
    }
    
    override hashCode() {
        super.hashCode + list.hashCode
    }
}