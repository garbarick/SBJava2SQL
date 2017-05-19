package com.netcracker.java2sql;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;

/**
 * SEBY0408
 */
public class ActionHandler extends EditorActionHandler
{
    private static final Logger log = Logger.getInstance(ActionHandler.class.getName());
    private Class psiPolyadicExpressionClass;
    private List<PsiElement> elements = new ArrayList<PsiElement>();

    public ActionHandler()
    {
        psiPolyadicExpressionClass = getPsiPolyadicExpressionClass();
    }

    @Override
    public void execute(Editor editor, DataContext dataContext)
    {
        elements.clear();
        SelectionModel selectionModel = editor.getSelectionModel();
        PsiFile psiFile = (PsiFile) dataContext.getData(LangDataKeys.PSI_FILE.getName());

        if (selectionModel.hasSelection())
        {
            findInSelection(selectionModel, psiFile);
        }
        else
        {
            findInPosition(editor, psiFile);
        }

        if (elements.isEmpty())
        {
            statusBarInfo(psiFile, "SBJava2SQL: failed");
        }
        else
        {
            CopyPasteManager.getInstance().setContents(new StringSelection(getValue()));
            statusBarInfo(psiFile, "SBJava2SQL: copied");
        }
    }

    private void findInSelection(SelectionModel selectionModel, PsiFile psiFile)
    {
        PsiElement startElement = psiFile.findElementAt(selectionModel.getSelectionStart());
        PsiElement endElement = psiFile.findElementAt(selectionModel.getSelectionEnd());

        int startOffset = startElement != null ? startElement.getTextOffset() : 0;
        int endOffset = endElement != null ? endElement.getTextRange().getEndOffset() : 0;

        PsiElement element = PsiTreeUtil.findElementOfClassAtRange(psiFile, startOffset, endOffset, PsiBinaryExpression.class);
        if (element == null && psiPolyadicExpressionClass != null)
        {
            element = PsiTreeUtil.findElementOfClassAtRange(psiFile, startOffset, endOffset, psiPolyadicExpressionClass);
        }
        if (element == null)
        {
            element = PsiTreeUtil.findElementOfClassAtRange(psiFile, startOffset, endOffset, PsiLiteralExpression.class);
        }
        if (element == null)
        {
            element = PsiTreeUtil.findElementOfClassAtRange(psiFile, startOffset, endOffset, PsiElement.class);
            if (element != null && element.getParent() != null)
            {
                for (PsiElement child : element.getParent().getChildren())
                {
                    if (child.getTextOffset() >= startOffset && child.getTextOffset() < endOffset)
                    {
                        addElement(child);
                    }
                }
            }
        }
        addElement(element);
    }

    private void findInPosition(Editor editor, PsiFile psiFile)
    {
        PsiElement element = PsiTreeUtil.getParentOfType(psiFile.findElementAt(editor.getCaretModel().getOffset()), PsiBinaryExpression.class, false, new Class[]{PsiMember.class});
        if (element != null)
        {
            do
            {
                PsiElement parentExpression = element.getParent();
                if (!(parentExpression instanceof PsiBinaryExpression))
                {
                    break;
                }
                element = parentExpression;
            }
            while (true);
        }
        if (element == null && psiPolyadicExpressionClass != null)
        {
            element = PsiTreeUtil.getParentOfType(psiFile.findElementAt(editor.getCaretModel().getOffset()), psiPolyadicExpressionClass);
        }
        if (element == null)
        {
            element = PsiTreeUtil.getParentOfType(psiFile.findElementAt(editor.getCaretModel().getOffset()), PsiLiteralExpression.class);
        }
        addElement(element);
    }

    private String getValue()
    {
        StringBuilder buffer = new StringBuilder();
        for (PsiElement element : elements)
        {
            buffer.append(getValue(element));
        }
        return buffer.toString();
    }

    private String getValue(PsiElement element)
    {
        StringBuilder buffer = new StringBuilder();
        if (element instanceof PsiReferenceExpression)
        {
            PsiElement resolve = ((PsiReferenceExpression) element).resolve();
            buffer.append(getValue(resolve));
        }
        else if (element instanceof PsiLiteralExpression)
        {
            buffer.append(((PsiLiteralExpression) element).getValue());
        }
        else if (element instanceof PsiMethodCallExpression)
        {
            buffer.append(getValue(element));
        }
        else if (element instanceof PsiIdentifier)
        {
            if (element.getParent() instanceof PsiField)
            {
                buffer.append("/*").append(element.getText()).append("*/");
            }
        }
        else if (element instanceof PsiJavaToken)
        {
        }
        else if (element instanceof PsiTypeElement)
        {
        }
        else
        {
            buffer.append(getChildrenValue(element));
        }
        return buffer.toString();
    }

    private String getChildrenValue(PsiElement element)
    {
        StringBuilder buffer = new StringBuilder();
        PsiElement[] children = element.getChildren();
        for (PsiElement child : children)
        {
            buffer.append(getValue(child));
        }
        return buffer.toString();
    }

    private Class getPsiPolyadicExpressionClass()
    {
        try
        {
            return Thread.currentThread().getContextClassLoader().loadClass("com.intellij.psi.PsiPolyadicExpression");
        }
        catch (ClassNotFoundException e)
        {
            return null;
        }
    }

    private void addElement(PsiElement element)
    {
        if (element != null)
        {
            elements.add(element);
        }
    }

    private void statusBarInfo(PsiFile psiFile, String message)
    {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(psiFile.getProject());
        if (statusBar != null)
        {
            statusBar.setInfo(message);
        }
    }
}
