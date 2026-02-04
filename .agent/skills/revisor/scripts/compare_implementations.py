#!/usr/bin/env python3
"""
Compare Implementations Script
Compares multiple AI-generated implementations of the same task side-by-side.
"""

import argparse
import json
import sys
from pathlib import Path
from datetime import datetime
import difflib

class ImplementationComparator:
    """Compare multiple code implementations."""
    
    def __init__(self, impl_paths):
        self.implementations = []
        for path in impl_paths:
            self.implementations.append({
                'path': Path(path),
                'name': Path(path).stem,
                'content': self._load_file(path),
                'metrics': {}
            })
    
    def _load_file(self, filepath):
        """Load file content safely."""
        try:
            with open(filepath, 'r', encoding='utf-8') as f:
                return f.read()
        except Exception as e:
            print(f"Warning: Could not load {filepath}: {e}")
            return ""
    
    def analyze(self):
        """Analyze all implementations."""
        for impl in self.implementations:
            self._calculate_metrics(impl)
        
        return self.implementations
    
    def _calculate_metrics(self, impl):
        """Calculate basic metrics for an implementation."""
        content = impl['content']
        lines = content.split('\n')
        
        # Basic metrics
        impl['metrics']['total_lines'] = len(lines)
        impl['metrics']['code_lines'] = len([l for l in lines if l.strip() and not l.strip().startswith('#') and not l.strip().startswith('//')])
        impl['metrics']['blank_lines'] = len([l for l in lines if not l.strip()])
        impl['metrics']['comment_lines'] = len([l for l in lines if l.strip().startswith('#') or l.strip().startswith('//')])
        
        # Character count
        impl['metrics']['characters'] = len(content)
        
        # Average line length
        code_lines = [l for l in lines if l.strip()]
        impl['metrics']['avg_line_length'] = sum(len(l) for l in code_lines) / len(code_lines) if code_lines else 0
        
        # Complexity indicators (rough estimates)
        impl['metrics']['if_statements'] = content.count('if ')
        impl['metrics']['loops'] = content.count('for ') + content.count('while ')
        impl['metrics']['functions'] = content.count('def ') + content.count('function ')
        impl['metrics']['classes'] = content.count('class ')
    
    def generate_comparison_report(self, output_file):
        """Generate detailed comparison report."""
        report_lines = []
        
        # Header
        report_lines.append("# Implementation Comparison Report")
        report_lines.append("")
        report_lines.append(f"**Comparison Date:** {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        report_lines.append(f"**Implementations Compared:** {len(self.implementations)}")
        report_lines.append("")
        report_lines.append("---")
        report_lines.append("")
        
        # Quick Overview
        report_lines.append("## Quick Overview")
        report_lines.append("")
        report_lines.append("| Implementation | Lines | Code Lines | Functions | Complexity Score |")
        report_lines.append("|----------------|-------|------------|-----------|------------------|")
        
        for impl in self.implementations:
            m = impl['metrics']
            complexity = m['if_statements'] + m['loops'] * 2 + m['classes'] * 3
            report_lines.append(
                f"| {impl['name']} | {m['total_lines']} | {m['code_lines']} | "
                f"{m['functions']} | {complexity} |"
            )
        
        report_lines.append("")
        report_lines.append("---")
        report_lines.append("")
        
        # Detailed Metrics Comparison
        report_lines.append("## Detailed Metrics")
        report_lines.append("")
        
        for impl in self.implementations:
            report_lines.append(f"### {impl['name']}")
            report_lines.append("")
            report_lines.append(f"**File:** `{impl['path']}`")
            report_lines.append("")
            
            m = impl['metrics']
            report_lines.append("**Metrics:**")
            report_lines.append(f"- Total Lines: {m['total_lines']}")
            report_lines.append(f"- Code Lines: {m['code_lines']}")
            report_lines.append(f"- Comment Lines: {m['comment_lines']}")
            report_lines.append(f"- Blank Lines: {m['blank_lines']}")
            report_lines.append(f"- Average Line Length: {m['avg_line_length']:.1f} characters")
            report_lines.append(f"- Functions: {m['functions']}")
            report_lines.append(f"- Classes: {m['classes']}")
            report_lines.append(f"- Conditionals (if): {m['if_statements']}")
            report_lines.append(f"- Loops (for/while): {m['loops']}")
            report_lines.append("")
        
        report_lines.append("---")
        report_lines.append("")
        
        # Side-by-Side Comparison
        if len(self.implementations) == 2:
            report_lines.append("## Side-by-Side Diff")
            report_lines.append("")
            report_lines.append("```diff")
            
            impl1_lines = self.implementations[0]['content'].split('\n')
            impl2_lines = self.implementations[1]['content'].split('\n')
            
            diff = list(difflib.unified_diff(
                impl1_lines,
                impl2_lines,
                fromfile=self.implementations[0]['name'],
                tofile=self.implementations[1]['name'],
                lineterm=''
            ))
            
            # Limit diff output to first 100 lines
            for line in diff[:100]:
                report_lines.append(line)
            
            if len(diff) > 100:
                report_lines.append(f"... ({len(diff) - 100} more lines)")
            
            report_lines.append("```")
            report_lines.append("")
            report_lines.append("---")
            report_lines.append("")
        
        # Pros and Cons Analysis
        report_lines.append("## Comparative Analysis")
        report_lines.append("")
        
        # Find best in each category
        best_readable = min(self.implementations, key=lambda x: x['metrics']['avg_line_length'])
        best_concise = min(self.implementations, key=lambda x: x['metrics']['code_lines'])
        best_documented = max(self.implementations, key=lambda x: x['metrics']['comment_lines'])
        
        report_lines.append("### Strengths by Implementation")
        report_lines.append("")
        
        for impl in self.implementations:
            report_lines.append(f"#### {impl['name']}")
            report_lines.append("")
            
            strengths = []
            
            if impl == best_readable:
                strengths.append("✅ Most readable (shortest average line length)")
            if impl == best_concise:
                strengths.append("✅ Most concise (fewest lines of code)")
            if impl == best_documented:
                strengths.append("✅ Best documented (most comments)")
            
            # Additional analysis
            m = impl['metrics']
            if m['functions'] > 5:
                strengths.append("✅ Good modularization (multiple functions)")
            if m['classes'] > 0:
                strengths.append("✅ Object-oriented design")
            if m['comment_lines'] / m['code_lines'] > 0.1 if m['code_lines'] > 0 else False:
                strengths.append("✅ Well-commented (>10% comment ratio)")
            
            if strengths:
                for strength in strengths:
                    report_lines.append(strength)
            else:
                report_lines.append("_No standout strengths identified_")
            
            report_lines.append("")
            
            # Potential concerns
            concerns = []
            
            if m['avg_line_length'] > 100:
                concerns.append("⚠️ Long lines may impact readability")
            if m['code_lines'] > 500:
                concerns.append("⚠️ Large file - consider splitting")
            if m['comment_lines'] == 0:
                concerns.append("⚠️ No comments - may be hard to understand")
            if m['functions'] == 0:
                concerns.append("⚠️ No functions - lack of modularity")
            
            complexity = m['if_statements'] + m['loops'] * 2
            if complexity > 20:
                concerns.append("⚠️ High cyclomatic complexity")
            
            if concerns:
                report_lines.append("**Concerns:**")
                for concern in concerns:
                    report_lines.append(concern)
                report_lines.append("")
        
        report_lines.append("---")
        report_lines.append("")
        
        # Recommendations
        report_lines.append("## Recommendations")
        report_lines.append("")
        
        # Determine winner based on multiple factors
        scores = []
        for impl in self.implementations:
            m = impl['metrics']
            score = 0
            
            # Prefer concise code (but not too concise)
            if 50 <= m['code_lines'] <= 300:
                score += 2
            
            # Prefer well-documented code
            comment_ratio = m['comment_lines'] / m['code_lines'] if m['code_lines'] > 0 else 0
            if comment_ratio > 0.1:
                score += 2
            
            # Prefer modular code
            if m['functions'] >= 3:
                score += 2
            
            # Prefer readable line lengths
            if 40 <= m['avg_line_length'] <= 80:
                score += 1
            
            # Penalize high complexity
            complexity = m['if_statements'] + m['loops'] * 2
            if complexity > 30:
                score -= 2
            
            scores.append((impl['name'], score))
        
        scores.sort(key=lambda x: x[1], reverse=True)
        
        report_lines.append("### Recommended Implementation")
        report_lines.append("")
        report_lines.append(f"Based on metrics analysis, **{scores[0][0]}** appears to be the best overall implementation.")
        report_lines.append("")
        report_lines.append("**Ranking:**")
        for i, (name, score) in enumerate(scores, 1):
            report_lines.append(f"{i}. **{name}** (score: {score})")
        report_lines.append("")
        report_lines.append("**Note:** This is an automated analysis based on metrics only. ")
        report_lines.append("Manual review of correctness, logic, and specific requirements is essential.")
        report_lines.append("")
        
        # Next steps
        report_lines.append("---")
        report_lines.append("")
        report_lines.append("## Next Steps")
        report_lines.append("")
        report_lines.append("1. **Test all implementations** with identical test cases")
        report_lines.append("2. **Measure performance** (execution time, memory usage)")
        report_lines.append("3. **Review for correctness** - metrics don't guarantee correct logic")
        report_lines.append("4. **Check edge cases** - ensure all handle boundary conditions")
        report_lines.append("5. **Consider combining strengths** - take best parts from each")
        report_lines.append("")
        report_lines.append("---")
        report_lines.append("")
        report_lines.append("_This report was automatically generated by Implementation Comparator_")
        
        # Write report
        report_content = "\n".join(report_lines)
        with open(output_file, 'w') as f:
            f.write(report_content)
        
        return report_content
    
    def print_summary(self):
        """Print quick summary to console."""
        print("\n" + "="*60)
        print("IMPLEMENTATION COMPARISON SUMMARY")
        print("="*60)
        
        for impl in self.implementations:
            m = impl['metrics']
            print(f"\n{impl['name']}:")
            print(f"  Lines: {m['code_lines']} (code) + {m['comment_lines']} (comments)")
            print(f"  Functions: {m['functions']}")
            print(f"  Complexity: {m['if_statements']} conditionals, {m['loops']} loops")
        
        print("\n" + "="*60)


def main():
    parser = argparse.ArgumentParser(
        description="Compare multiple AI-generated implementations",
        epilog="Example: python compare_implementations.py --impl1 agent_a.py --impl2 agent_b.py --output comparison.md"
    )
    parser.add_argument('--impl1', required=True, help="Path to first implementation")
    parser.add_argument('--impl2', required=True, help="Path to second implementation")
    parser.add_argument('--impl3', help="Path to third implementation (optional)")
    parser.add_argument('--impl4', help="Path to fourth implementation (optional)")
    parser.add_argument('--output', default='comparison_report.md', help="Output file for comparison report")
    
    args = parser.parse_args()
    
    # Collect all implementation paths
    impl_paths = [args.impl1, args.impl2]
    if args.impl3:
        impl_paths.append(args.impl3)
    if args.impl4:
        impl_paths.append(args.impl4)
    
    # Verify all files exist
    for path in impl_paths:
        if not Path(path).exists():
            print(f"Error: File not found: {path}")
            sys.exit(1)
    
    # Create comparator and analyze
    comparator = ImplementationComparator(impl_paths)
    comparator.analyze()
    
    # Generate report
    comparator.generate_comparison_report(args.output)
    
    # Print summary
    comparator.print_summary()
    
    print(f"\n✅ Detailed comparison report generated: {args.output}")

if __name__ == "__main__":
    main()
