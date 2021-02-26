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
public interface IFile extends Serializable {

    /**
     * Setter for <code>texera_db.file.uid</code>.
     */
    public void setUid(UInteger value);

    /**
     * Getter for <code>texera_db.file.uid</code>.
     */
    public UInteger getUid();

    /**
     * Setter for <code>texera_db.file.fid</code>.
     */
    public void setFid(UInteger value);

    /**
     * Getter for <code>texera_db.file.fid</code>.
     */
    public UInteger getFid();

    /**
     * Setter for <code>texera_db.file.size</code>.
     */
    public void setSize(UInteger value);

    /**
     * Getter for <code>texera_db.file.size</code>.
     */
    public UInteger getSize();

    /**
     * Setter for <code>texera_db.file.name</code>.
     */
    public void setName(String value);

    /**
     * Getter for <code>texera_db.file.name</code>.
     */
    public String getName();

    /**
     * Setter for <code>texera_db.file.path</code>.
     */
    public void setPath(String value);

    /**
     * Getter for <code>texera_db.file.path</code>.
     */
    public String getPath();

    /**
     * Setter for <code>texera_db.file.description</code>.
     */
    public void setDescription(String value);

    /**
     * Getter for <code>texera_db.file.description</code>.
     */
    public String getDescription();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common interface IFile
     */
    public void from(IFile from);

    /**
     * Copy data into another generated Record/POJO implementing the common interface IFile
     */
    public <E extends IFile> E into(E into);
}
