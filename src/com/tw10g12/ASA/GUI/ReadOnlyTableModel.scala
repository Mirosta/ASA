package com.tw10g12.ASA.GUI

import javax.swing.table.DefaultTableModel

/**
 * Created by Tom on 18/04/2015.
 */
class ReadOnlyTableModel(data: Array[Array[AnyRef]], columnNames: Array[AnyRef]) extends DefaultTableModel(data, columnNames)
{
    override def isCellEditable(row: Int, column: Int): Boolean =
    {
        return false
    }
}
