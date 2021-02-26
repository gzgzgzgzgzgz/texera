/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.dataflow.jooq.generated.tables.interfaces;


import java.io.Serializable;

import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public interface IKeywordDictionary extends Serializable {

    /**
     * Setter for <code>texera_db.keyword_dictionary.uid</code>.
     */
    public void setUid(UInteger value);

    /**
     * Getter for <code>texera_db.keyword_dictionary.uid</code>.
     */
    public UInteger getUid();

    /**
     * Setter for <code>texera_db.keyword_dictionary.kid</code>.
     */
    public void setKid(UInteger value);

    /**
     * Getter for <code>texera_db.keyword_dictionary.kid</code>.
     */
    public UInteger getKid();

    /**
     * Setter for <code>texera_db.keyword_dictionary.name</code>.
     */
    public void setName(String value);

    /**
     * Getter for <code>texera_db.keyword_dictionary.name</code>.
     */
    public String getName();

    /**
     * Setter for <code>texera_db.keyword_dictionary.content</code>.
     */
    public void setContent(byte[] value);

    /**
     * Getter for <code>texera_db.keyword_dictionary.content</code>.
     */
    public byte[] getContent();

    /**
     * Setter for <code>texera_db.keyword_dictionary.description</code>.
     */
    public void setDescription(String value);

    /**
     * Getter for <code>texera_db.keyword_dictionary.description</code>.
     */
    public String getDescription();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common interface IKeywordDictionary
     */
    public void from(IKeywordDictionary from);

    /**
     * Copy data into another generated Record/POJO implementing the common interface IKeywordDictionary
     */
    public <E extends IKeywordDictionary> E into(E into);
}
