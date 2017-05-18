package com.netcracker.java2sql;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

import java.awt.datatransfer.StringSelection;

public class SBJava2SQLAction extends EditorAction
{
    private static final Logger log = Logger.getInstance(SBJava2SQLAction.class.getName());

    public SBJava2SQLAction()
    {
        super(new EditorActionHandler()
        {
            @Override
            public void execute(Editor editor, DataContext dataContext)
            {
                PsiElement element = null;
                SelectionModel selectionModel = editor.getSelectionModel();
                PsiFile psiFile = (PsiFile) dataContext.getData(LangDataKeys.PSI_FILE.getName());

                if (selectionModel.hasSelection())
                {
                    int startOffset = psiFile.findElementAt(selectionModel.getSelectionStart()).getTextOffset();
                    int endOffset = psiFile.findElementAt(selectionModel.getSelectionEnd()).getTextRange().getEndOffset();
                    element = PsiTreeUtil.findElementOfClassAtRange(psiFile, startOffset, endOffset, PsiBinaryExpression.class);
                    if (element == null)
                    {
                        element = PsiTreeUtil.findElementOfClassAtRange(psiFile, startOffset, endOffset, PsiLiteralExpression.class);
                    }
                }
                else
                {
                    element = PsiTreeUtil.getParentOfType(psiFile.findElementAt(editor.getCaretModel().getOffset()), PsiBinaryExpression.class, false, new Class[]{PsiMember.class});
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
                    else
                    {
                        element = PsiTreeUtil.getParentOfType(psiFile.findElementAt(editor.getCaretModel().getOffset()), PsiLiteralExpression.class);
                    }
                }

                if (element != null)
                {
                    CopyPasteManager.getInstance().setContents(new StringSelection(getValue(element)));
                    WindowManager.getInstance().getStatusBar(psiFile.getProject()).setInfo("SBJava2SQL: copied");
                }
                else
                {
                    WindowManager.getInstance().getStatusBar(psiFile.getProject()).setInfo("SBJava2SQL: failed");
                }
            }

            private String getValue(PsiElement element)
            {
                StringBuilder buffer = new StringBuilder();
                PsiElement[] childs = element.getChildren();
                if (childs != null)
                {
                    for (PsiElement child : childs)
                    {
                        if (child instanceof PsiReferenceExpression)
                        {
                            PsiElement resolve = ((PsiReferenceExpression) child).resolve();
                            buffer.append(getValue(resolve));
                        }
                        else if (child instanceof PsiLiteralExpression)
                        {
                            buffer.append(((PsiLiteralExpression) child).getValue());
                        }
                        else if (child instanceof PsiMethodCallExpression)
                        {
                            buffer.append(getValue(child));
                        }
                        else if (child instanceof PsiIdentifier)
                        {
                            if (child.getParent() instanceof PsiField)
                            {
                                buffer.append("/*").append(child.getText()).append("*/");
                            }
                        }
                        else if (child instanceof PsiTypeElement)
                        {
                        }
                        else
                        {
                            buffer.append(getValue(child));
                        }
                    }
                }
                return buffer.toString();
            }
        });
    }
}
