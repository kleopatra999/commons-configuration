/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This Configuration class allows you to add multiple different types of Configuration
 * to this CompositeConfiguration.  If you add Configuration1, and then Configuration2, 
 * any properties shared will mean that Configuration1 will be returned.
 * You can add multiple different types or the same type of properties file.
 * If Configuration1 doesn't have the property, then Configuration2 will be checked.
 * 
 * @version $Id: CompositeConfiguration.java,v 1.12 2004/06/04 12:58:53 ebourg Exp $
 */
public class CompositeConfiguration extends AbstractConfiguration
{
    /** List holding all the configuration */
    private List configList = new LinkedList();

    /** 
     * Configuration that holds in memory stuff.  Inserted as first so any
     * setProperty() override anything else added. 
     */
    private Configuration inMemoryConfiguration;

    /**
     * Creates an empty CompositeConfiguration object which can then
     * be added some other Configuration files
     */
    public CompositeConfiguration()
    {
        clear();
    }
    
    /**
     * Creates an CompositeConfiguration object with a specified InMemory
     * configuration.  This configuration will store any changes made to
     * the CompositeConfiguration.
     */
    public CompositeConfiguration(Configuration inMemoryConfiguration)
    {
        configList.clear();
        this.inMemoryConfiguration = inMemoryConfiguration;
        configList.add(inMemoryConfiguration);
    }    

    public void addConfiguration(Configuration config)
    {
        if (!configList.contains(config))
        {
            // As the inMemoryConfiguration contains all manually added keys,
            // we must make sure that it is always last. "Normal", non composed
            // configuration add their keys at the end of the configuration and
            // we want to mimic this behaviour.
            configList.add(configList.indexOf(inMemoryConfiguration), config);
        }
    }

    public void removeConfiguration(Configuration config)
    {
        // Make sure that you can't remove the inMemoryConfiguration from
        // the CompositeConfiguration object
        if (!config.equals(inMemoryConfiguration))
        {
            configList.remove(config);
        }
    }

    public int getNumberOfConfigurations()
    {
        return configList.size();
    }

    public void clear()
    {
        configList.clear();
        // recreate the in memory configuration
        inMemoryConfiguration = new BaseConfiguration();
        configList.add(inMemoryConfiguration);
    }
    
    /**
     * Add this property to the inmemory Configuration.
     *
     * @param key The Key to add the property to.
     * @param token The Value to add.
     */
    protected void addPropertyDirect(String key, Object token)
    {
        inMemoryConfiguration.addProperty(key, token);
    }    
    
    /**
     * Read property from underlying composite
     *
     * @param key key to use for mapping
     *
     * @return object associated with the given configuration key.
     */
    protected Object getPropertyDirect(String key) 
    {
        Configuration firstMatchingConfiguration = null;
        for (Iterator i = configList.iterator(); i.hasNext();)
        {
            Configuration config = (Configuration) i.next();
            if (config.containsKey(key))
            {
                firstMatchingConfiguration = config;
                break;
            }
        }

        if (firstMatchingConfiguration != null)
        {
            return firstMatchingConfiguration.getProperty(key);
        }
        else
        {
            return null;
        }
        /*throw new NoSuchElementException(
            '\'' + key + "' doesn't map to an existing object");*/
    }

    /**
     * Get the list of the keys contained in the configuration
     * repository.
     *
     * @return An Iterator.
     */
    public Iterator getKeys()
    {
        List keys = new ArrayList();
        for (Iterator i = configList.iterator(); i.hasNext();)
        {
            Configuration config = (Configuration) i.next();
            for (Iterator j = config.getKeys(); j.hasNext();)
            {
                String key = (String) j.next();
                if (!keys.contains(key))
                {
                    keys.add(key);
                }
            }
        }
        return keys.iterator();
    }

    /**
     * Get the list of the keys contained in the configuration
     * repository.
     *
     * @return An Iterator.
     */
    public Iterator getKeys(String key)
    {
        List keys = new ArrayList();
        for (Iterator i = configList.iterator(); i.hasNext();)
        {
            Configuration config = (Configuration) i.next();
            for (Iterator j = config.getKeys(key); j.hasNext();)
            {
                String newKey = (String) j.next();
                if (!keys.contains(newKey))
                {
                    keys.add(newKey);
                }
            }
        }
        return keys.iterator();
    }
   
    public boolean isEmpty()
    {
        boolean isEmpty = true;
        for (Iterator i = configList.iterator(); i.hasNext();)
        {
            Configuration config = (Configuration) i.next();
            if (!config.isEmpty())
            {
                return false;
            }
        }
        return isEmpty;
    }

    /**
     *  Gets a property from the configuration.
     *
     *  @param key property to retrieve
     *  @return value as object. Will return user value if exists,
     *          if not then default value if exists, otherwise null
     */
    public Object getProperty(String key)
    {
        return getPropertyDirect(key);
    }

    /**
     * Set a property, this will replace any previously
     * set values. Set values is implicitly a call
     * to clearProperty(key), addProperty(key,value).
     *
     * @param key
     * @param value
     */
    public void setProperty(String key, Object value)
    {
        clearProperty(key);
        addProperty(key, value);
    }

    /**
     * Clear a property in the configuration.
     *
     * @param key the key to remove along with corresponding value.
     */
    public void clearProperty(String key)
    {
        for (Iterator i = configList.iterator(); i.hasNext();)
        {
            Configuration config = (Configuration) i.next();
            config.clearProperty(key);
        }
    }

    /**
     * check if the configuration contains the key
     */
    public boolean containsKey(String key)
    {
        for (Iterator i = configList.iterator(); i.hasNext();)
        {
            Configuration config = (Configuration) i.next();
            if (config.containsKey(key))
            {
                return true;
            }
        }
        return false;
    }
 
    /**
     * Get a List of strings associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated List.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a List.
     */
    public List getList(String key)
    {
        List list = new ArrayList();

        // add all elements from the first configuration containing the requested key
        Iterator it = configList.iterator();
        while (it.hasNext() && list.isEmpty())
        {
            Configuration config = (Configuration) it.next();
            if (config != inMemoryConfiguration && config.containsKey(key))
            {
                list.addAll(config.getList(key));
            }
        }

        // add all elements from the in memory configuration
        list.addAll(inMemoryConfiguration.getList(key, null));

        return list;
    }

    /**
     * Get a List of strings associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated List.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a List.
     */
    public List getList(String key, List defaultValue)
    {
        List list = getList(key);

        return list.isEmpty() ? defaultValue : list;
    }
    
    /**
     * Get an array of strings associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated string array if key is found.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a String/List of Strings.
     */
    public String[] getStringArray(String key)
    {
        List list = getList(key);
        return (String []) list.toArray(new String [0]);
    }    


    public Configuration getConfiguration(int index)
    {
        return (Configuration) configList.get(index);
    }

    /**
     * @return Returns the inMemoryConfiguration.
     */
    public Configuration getInMemoryConfiguration() {
        return inMemoryConfiguration;
    }
}
