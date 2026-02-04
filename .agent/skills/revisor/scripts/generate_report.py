#!/usr/bin/env python3
"""
Code Review Report Generator
Combines static analysis results with manual review notes into a structured report.
"""

import json
import argparse
from datetime import datetime
from pathlib import Path

def load_json(filepath):
    """Load JSON file safely."""
    try:
        with open(filepath, 'r') as f:
            return json.load(f)
    except:
        return {}

def load_markdown(filepath):
    """Load markdown file safely."""
    try:
        with open(filepath, 'r') as f:
            return f.read()
    except:
        return ""

def severity_emoji(severity):
    """Get emoji for severity level."""
    severity_map = {
        'critical': '🚨',
        'high': '🚨',
        'error': '🚨',
        'major': '⚠️',
        'warning': '⚠️',
        'medium': '⚠️',
        'minor': '💡',
        'info': '💡',
        'low': '💡'
    }
    return severity_map.get(severity.lower(), '❓')

def generate_report(static_analysis, manual_review, output_file):
    """Generate comprehensive code review report."""
    
    report_lines = []
    
    # Header
    report_lines.append("# Code Review Report")
    report_lines.append("")
    report_lines.append(f"**Review Date:** {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    report_lines.append(f"**Reviewer:** AI Code Reviewer")
    report_lines.append("")
    report_lines.append("---")
    report_lines.append("")
    
    # Executive Summary
    report_lines.append("## Executive Summary")
    report_lines.append("")
    
    if static_analysis:
        summary = static_analysis.get('summary', {})
        total_issues = summary.get('total_issues', 0)
        security_issues = summary.get('security_issues', 0)
        
        if total_issues == 0 and security_issues == 0:
            report_lines.append("✅ **No automated issues detected.** Code appears to follow basic quality standards.")
        else:
            report_lines.append(f"Automated analysis identified **{total_issues} code quality issues** and **{security_issues} security concerns**.")
        
        report_lines.append("")
        
        # Quick stats
        report_lines.append("**Quick Statistics:**")
        report_lines.append(f"- Total Issues: {total_issues}")
        report_lines.append(f"- Errors: {summary.get('errors', 0)}")
        report_lines.append(f"- Warnings: {summary.get('warnings', 0)}")
        report_lines.append(f"- Security Issues: {security_issues}")
        
        metrics = static_analysis.get('metrics', {})
        if metrics:
            report_lines.append(f"- Files Analyzed: {metrics.get('total_files', 0)}")
            report_lines.append(f"- Lines of Code: {metrics.get('total_lines', 0)}")
    
    report_lines.append("")
    report_lines.append("---")
    report_lines.append("")
    
    # Critical Issues
    report_lines.append("## Critical Issues 🚨")
    report_lines.append("")
    
    critical_issues = []
    if static_analysis:
        # From security analysis
        for issue in static_analysis.get('security', []):
            if issue.get('severity', '').lower() in ['high', 'critical']:
                critical_issues.append(issue)
        
        # From code issues
        for issue in static_analysis.get('issues', []):
            if issue.get('severity', '').lower() in ['error', 'critical', 'high']:
                critical_issues.append(issue)
    
    if critical_issues:
        for i, issue in enumerate(critical_issues[:10], 1):  # Limit to top 10
            report_lines.append(f"### {i}. {issue.get('message', 'Issue detected')}")
            report_lines.append(f"**Location:** `{issue.get('file', 'unknown')}:{issue.get('line', 0)}`  ")
            report_lines.append(f"**Tool:** {issue.get('tool', 'unknown')}  ")
            report_lines.append(f"**Severity:** {issue.get('severity', 'unknown')}")
            report_lines.append("")
    else:
        report_lines.append("✅ No critical issues detected by automated analysis.")
    
    report_lines.append("")
    report_lines.append("---")
    report_lines.append("")
    
    # Major Concerns
    report_lines.append("## Major Concerns ⚠️")
    report_lines.append("")
    
    major_issues = []
    if static_analysis:
        for issue in static_analysis.get('issues', []):
            if issue.get('severity', '').lower() in ['warning', 'major', 'medium']:
                major_issues.append(issue)
    
    if major_issues:
        # Group by file
        issues_by_file = {}
        for issue in major_issues:
            filepath = issue.get('file', 'unknown')
            if filepath not in issues_by_file:
                issues_by_file[filepath] = []
            issues_by_file[filepath].append(issue)
        
        for filepath, issues in list(issues_by_file.items())[:5]:  # Top 5 files
            report_lines.append(f"### File: `{filepath}`")
            for issue in issues[:3]:  # Top 3 issues per file
                report_lines.append(f"- **Line {issue.get('line', 0)}**: {issue.get('message', '')}")
            if len(issues) > 3:
                report_lines.append(f"- _(... and {len(issues) - 3} more issues)_")
            report_lines.append("")
    else:
        report_lines.append("✅ No major concerns detected.")
    
    report_lines.append("")
    report_lines.append("---")
    report_lines.append("")
    
    # Manual Review Notes
    if manual_review:
        report_lines.append("## Manual Review Notes")
        report_lines.append("")
        report_lines.append(manual_review)
        report_lines.append("")
        report_lines.append("---")
        report_lines.append("")
    
    # Detailed Findings
    report_lines.append("## Detailed Analysis Results")
    report_lines.append("")
    
    if static_analysis:
        report_lines.append("### Code Quality Issues")
        report_lines.append("")
        
        all_issues = static_analysis.get('issues', [])
        if all_issues:
            report_lines.append("| File | Line | Severity | Message | Tool |")
            report_lines.append("|------|------|----------|---------|------|")
            
            for issue in all_issues[:30]:  # Limit to 30 for readability
                file_short = Path(issue.get('file', 'unknown')).name
                report_lines.append(
                    f"| {file_short} | {issue.get('line', 0)} | "
                    f"{severity_emoji(issue.get('severity', ''))} {issue.get('severity', '')} | "
                    f"{issue.get('message', '')[:50]}... | {issue.get('tool', '')} |"
                )
            
            if len(all_issues) > 30:
                report_lines.append("")
                report_lines.append(f"_({len(all_issues) - 30} more issues not shown)_")
        else:
            report_lines.append("No code quality issues detected.")
        
        report_lines.append("")
        
        # Security Issues
        report_lines.append("### Security Analysis")
        report_lines.append("")
        
        security = static_analysis.get('security', [])
        if security:
            report_lines.append("| File | Line | Severity | Message |")
            report_lines.append("|------|------|----------|---------|")
            
            for issue in security:
                file_short = Path(issue.get('file', 'unknown')).name
                report_lines.append(
                    f"| {file_short} | {issue.get('line', 0)} | "
                    f"{severity_emoji(issue.get('severity', ''))} {issue.get('severity', '')} | "
                    f"{issue.get('message', '')[:60]}... |"
                )
        else:
            report_lines.append("✅ No security issues detected by automated analysis.")
        
        report_lines.append("")
    
    # Recommendations
    report_lines.append("---")
    report_lines.append("")
    report_lines.append("## Recommendations")
    report_lines.append("")
    
    if static_analysis:
        summary = static_analysis.get('summary', {})
        
        if summary.get('security_high', 0) > 0:
            report_lines.append("### Immediate Actions (Critical)")
            report_lines.append(f"1. Address {summary.get('security_high', 0)} high-severity security issues")
            report_lines.append("2. Review and fix all critical errors before deployment")
            report_lines.append("")
        
        if summary.get('errors', 0) > 0 or summary.get('warnings', 0) > 5:
            report_lines.append("### Short-term Improvements")
            if summary.get('errors', 0) > 0:
                report_lines.append(f"1. Fix {summary.get('errors', 0)} error-level issues")
            if summary.get('warnings', 0) > 5:
                report_lines.append(f"2. Address {summary.get('warnings', 0)} warnings to improve code quality")
            report_lines.append("")
        
        if summary.get('total_issues', 0) > 20:
            report_lines.append("### Long-term Enhancements")
            report_lines.append("1. Set up automated linting in CI/CD pipeline")
            report_lines.append("2. Implement pre-commit hooks for code quality")
            report_lines.append("3. Schedule regular security audits")
            report_lines.append("")
    
    # Approval Status
    report_lines.append("---")
    report_lines.append("")
    report_lines.append("## Approval Status")
    report_lines.append("")
    
    if static_analysis:
        summary = static_analysis.get('summary', {})
        security_high = summary.get('security_high', 0)
        errors = summary.get('errors', 0)
        
        if security_high > 0 or errors > 10:
            report_lines.append("- [ ] Approved")
            report_lines.append("- [ ] Approved with minor changes")
            report_lines.append("- [x] **Requires Revision** - Critical issues must be addressed")
            report_lines.append("- [ ] Requires major refactoring")
        elif errors > 0 or summary.get('warnings', 0) > 10:
            report_lines.append("- [ ] Approved")
            report_lines.append("- [x] **Approved with Changes** - Address errors and major warnings")
            report_lines.append("- [ ] Requires Revision")
            report_lines.append("- [ ] Requires major refactoring")
        else:
            report_lines.append("- [x] **Approved** - Code meets quality standards")
            report_lines.append("- [ ] Approved with minor changes")
            report_lines.append("- [ ] Requires Revision")
            report_lines.append("- [ ] Requires major refactoring")
    
    report_lines.append("")
    report_lines.append("---")
    report_lines.append("")
    
    # Footer
    report_lines.append("## Next Steps")
    report_lines.append("")
    report_lines.append("1. Review all critical and high-severity issues")
    report_lines.append("2. Implement recommended fixes")
    report_lines.append("3. Re-run static analysis to verify improvements")
    report_lines.append("4. Request follow-up review if significant changes made")
    report_lines.append("")
    report_lines.append("_This report was automatically generated by AI Code Reviewer_")
    
    # Write report
    report_content = "\n".join(report_lines)
    with open(output_file, 'w') as f:
        f.write(report_content)
    
    return report_content

def main():
    parser = argparse.ArgumentParser(description="Generate code review report")
    parser.add_argument("--static-analysis", help="Path to static analysis JSON file")
    parser.add_argument("--manual-review", help="Path to manual review markdown file")
    parser.add_argument("--output", default="code_review_report.md", help="Output file")
    
    args = parser.parse_args()
    
    static_data = load_json(args.static_analysis) if args.static_analysis else {}
    manual_notes = load_markdown(args.manual_review) if args.manual_review else ""
    
    report = generate_report(static_data, manual_notes, args.output)
    
    print(f"✅ Report generated successfully: {args.output}")
    print(f"📄 Report length: {len(report)} characters")

if __name__ == "__main__":
    main()
