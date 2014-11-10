package com.tw10g12.Maths;

public class Matrix4
{
    private double[][] elements = new double[4][4];

    public Matrix4(double[][] elements)
    {
        for(int y = 0; y < 4; y++)
        {
            for(int x = 0; x < 4; x++)
            {
                this.elements[y][x] = elements[y][x];
            }
        }
    }

    public Matrix4 multiply(Matrix4 m)
    {
        return Matrix4.multiply(this, m);
    }

    public Vector4 multiply(Vector4 v)
    {
        return Matrix4.multiply(this, v);
    }

    public double[][] getMatrixValues()
    {
        return elements;
    }

    public double[] flattenMatrixValues()
    {
        double[] values = new double[16];

        for(int y = 0; y < 4; y++)
        {
            for(int x = 0; x < 4; x++)
            {
                values[y + x*4] = elements[y][x];
            }
        }

        return values;
    }

    public static Matrix4 getIdentityMatrix()
    {
        return new Matrix4(new double[][]
                {
                        new double[] {1,0,0,0},
                        new double[] {0,1,0,0},
                        new double[] {0,0,1,0},
                        new double[] {0,0,0,1}
                });
    }

    public static Matrix4 getTranslationMatrix(Vector3 translation)
    {
        return new Matrix4(new double[][]
                {
                        new double[] {1,0,0,(double)(translation.getX())},
                        new double[] {0,1,0,(double)(translation.getY())},
                        new double[] {0,0,1,(double)(translation.getZ())},
                        new double[] {0,0,0,1}
                });
    }

    public static Matrix4 getScaleMatrix(double scale)
    {
        return new Matrix4(new double[][]
                {
                        new double[] {scale,0,0,0},
                        new double[] {0,scale,0,0},
                        new double[] {0,0,scale,0},
                        new double[] {0,0,0,1}
                });
    }

    public static Matrix4 getScaleMatrix(Vector3 scale)
    {
        return new Matrix4(new double[][]
                {
                        new double[] {scale.getX(),0,0,0},
                        new double[] {0,scale.getY(),0,0},
                        new double[] {0,0,scale.getZ(),0},
                        new double[] {0,0,0,1}
                });
    }

    private static double multiplyRow(double[] row1, double[] row2)
    {
        double newVal = 0;

        for(int i = 0; i < 4; i++)
        {
            newVal += row1[i]*row2[i];
        }

        return newVal;
    }

    private static double[] multiplyRows(double[][] rows, double[] row)
    {
        double[] newRow = new double[4];

        for(int i =0; i < 4; i++)
        {
            newRow[i] = multiplyRow(rows[i], row);
        }

        return newRow;
    }

    public static Vector4 multiply(Matrix4 m, Vector4 v)
    {
        double[] newRow = multiplyRows(m.getMatrixValues(), new double[]{v.x, v.y, v.z, v.w});
        return new Vector4(newRow[0], newRow[1], newRow[2], newRow[3]);
    }

    public static Matrix4 multiply(Matrix4 m1, Matrix4 m2)
    {
        double[][] newRows = new double[4][4];
        double[][] m2Elements = m2.getMatrixValues();

        for(int i = 0; i < 4; i++)
        {
            double[] row = multiplyRows(m1.getMatrixValues(), new double[]{m2Elements[0][i],m2Elements[1][i],m2Elements[2][i],m2Elements[3][i]});
            for(int y = 0; y < 4; y++)
            {
                newRows[y][i] = row[y];
            }
        }

        return new Matrix4(newRows);
    }


    public static Matrix4 getRotationX(double rot)
    {
        double cos = Math.cos(rot);
        double sin = Math.sin(rot);

        return new Matrix4(new double[][]
                {
                        {1, 0, 0, 0},
                        {0, cos, -sin, 0},
                        {0, sin, cos, 0},
                        {0, 0, 0, 1}
                });
    }

    public static Matrix4 getRotationY(double rot)
    {
        double cos = Math.cos(rot);
        double sin = Math.sin(rot);

        return new Matrix4(new double[][]
                {
                        {cos, 0, sin, 0},
                        {0, 1, 0, 0},
                        {-sin, 0, cos, 0},
                        {0, 0, 0, 1}
                });
    }

    public static Matrix4 getRotationZ(double rot)
    {
        double cos = Math.cos(rot);
        double sin = Math.sin(rot);

        return new Matrix4(new double[][]
                {
                        {cos, -sin, 0, 0},
                        {sin, cos, 0, 0},
                        {0, 0, 1, 0},
                        {0, 0, 0, 1}
                });
    }

    public static Matrix4 getFromOpenGl(double[] values)
    {
        double[][] matrixValues = new double[4][4];
        for(int i =0; i < 16; i++)
        {
            int x = (int)Math.floor((double)i / 4.0);
            int y = i % 4;
            matrixValues[y][x] = values[i];
        }
        return new Matrix4(matrixValues);
    }
}
