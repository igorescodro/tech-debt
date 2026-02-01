package com.escodro.techdebt.gradle.report

import kotlinx.html.HEAD
import kotlinx.html.style
import kotlinx.html.unsafe

/** Generates the CSS styles for the HTML report. */
internal class ReportStyle {

    /**
     * Appends the CSS styles to the HEAD element.
     *
     * @param head the HEAD element to append the styles
     */
    fun append(head: HEAD) {
        head.style {
            unsafe {
                +"""
                ${baseStyles()}
                ${summaryStyles()}
                ${actionStyles()}
                ${cardStyles()}
                ${responsiveStyles()}
                """
                    .trimIndent()
            }
        }
    }

    private fun baseStyles() =
        """
                body {
                    font-family: sans-serif;
                    background-color: #f3f3f3;
                    margin: 0;
                    padding: 20px;
                }
                h1 {
                    color: #333;
                }
    """
            .trimIndent()

    private fun summaryStyles() =
        """
                .summary-container {
                    display: flex;
                    gap: 20px;
                    margin-bottom: 30px;
                    flex-wrap: wrap;
                }
                .summary-box {
                    flex: 1;
                    min-width: 120px;
                    padding: 20px;
                    border-radius: 8px;
                    color: white;
                    text-align: center;
                    box-shadow: 0 2px 5px rgba(0,0,0,0.1);
                }
                .summary-box h2 {
                    margin: 0;
                    font-size: 32px;
                }
                .summary-box span {
                    font-size: 14px;
                    text-transform: uppercase;
                    letter-spacing: 1px;
                }
                .total { background-color: #4A90E2; }
                .high { background-color: #E35D5D; }
                .medium { background-color: #F5A623; }
                .low { background-color: #4CAF50; }
                .none { background-color: #9E9E9E; }
    """
            .trimIndent()

    private fun actionStyles() =
        """
                .action-container {
                    margin-bottom: 20px;
                    display: flex;
                    justify-content: flex-end;
                }
                .action-button {
                    padding: 8px 16px;
                    margin-left: 10px;
                    border: none;
                    border-radius: 4px;
                    background-color: #4CAF50;
                    color: white;
                    cursor: pointer;
                    font-size: 14px;
                }
                .action-button:hover {
                    background-color: #45a049;
                }
    """
            .trimIndent()

    private fun cardStyles() =
        """
        ${cardContainerStyles()}
        ${cardHeaderStyles()}
        ${cardExpansionStyles()}
        ${cardContentStyles()}
        """
            .trimIndent()

    private fun cardContainerStyles() =
        """
                .card {
                    background-color: #fff;
                    box-shadow: 0 2px 5px rgba(0,0,0,0.1);
                    border-radius: 8px;
                    margin-bottom: 12px;
                    overflow: hidden;
                    border-left: 5px solid #4CAF50;
                }
                
                details summary {
                    padding: 15px;
                    cursor: pointer;
                    list-style: none;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                }
                
                details summary::-webkit-details-marker {
                    display: none;
                }
    """
            .trimIndent()

    private fun cardHeaderStyles() =
        """
                .card-header {
                    display: flex;
                    flex-direction: column;
                    gap: 4px;
                    flex: 1;
                }
                
                .card-header-main {
                    display: flex;
                    align-items: center;
                    gap: 10px;
                    flex: 1;
                }
                
                .header-column {
                    flex: 1;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                }
                
                .column-small {
                    flex: 0 0 150px;
                }
                
                .column-medium {
                    flex: 0 0 455px;
                }
                
                .module-badge {
                    background-color: #eee;
                    padding: 2px 8px;
                    border-radius: 12px;
                    font-size: 12px;
                    font-weight: bold;
                    color: #666;
                }
                
                .symbol-name {
                    font-weight: bold;
                    color: #333;
                }
    """
            .trimIndent()

    private fun cardExpansionStyles() =
        """
                .expand-icon {
                    width: 16px;
                    height: 16px;
                    color: #999;
                    transition: transform 0.2s;
                    margin-left: 10px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                }
                
                .expand-icon::after {
                    content: '';
                    width: 100%;
                    height: 100%;
                    background-color: currentColor;
                    -webkit-mask: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6 1.41-1.41z"/></svg>') no-repeat center;
                    mask: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6 1.41-1.41z"/></svg>') no-repeat center;
                }
                
                details[open] .expand-icon {
                    transform: rotate(180deg);
                }
    """
            .trimIndent()

    private fun cardContentStyles() =
        """
                .card-content {
                    padding: 0 15px 15px 15px;
                    border-top: 1px solid #eee;
                    display: grid;
                    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
                    gap: 15px;
                    background-color: #fafafa;
                }
                
                .info-group {
                    display: flex;
                    flex-direction: column;
                    gap: 4px;
                    margin-top: 10px;
                }
                
                .info-label {
                    font-size: 11px;
                    text-transform: uppercase;
                    color: #999;
                    font-weight: bold;
                }
                
                .info-value {
                    font-size: 14px;
                    color: #333;
                }
                
                .ticket {
                    font-family: monospace;
                    background-color: #eee;
                    padding: 2px 4px;
                    border-radius: 4px;
                }
    """
            .trimIndent()

    private fun responsiveStyles() =
        """
                @media (max-width: 600px) {
                    body {
                        padding: 10px;
                    }
                    .summary-box {
                        padding: 10px;
                    }
                    .summary-box h2 {
                        font-size: 24px;
                    }
                    .card-header-main {
                        flex-direction: column;
                        align-items: flex-start;
                        gap: 4px;
                    }
                    .header-column {
                        flex: none;
                        width: 100%;
                    }
                    .column-small, .column-medium {
                        flex: none;
                        width: auto;
                    }
                }
    """
            .trimIndent()
}
